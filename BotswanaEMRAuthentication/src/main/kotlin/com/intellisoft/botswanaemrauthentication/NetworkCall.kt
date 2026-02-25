package com.intellisoft.botswanaemrauthentication

import kotlinx.coroutines.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import retrofit2.Retrofit
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.annotation.PostConstruct


@Component
class NetworkCall(
    @Autowired private val retrofit: Retrofit
) {

    @Autowired
    lateinit var appProperties: AppProperties

    var restTemplate = RestTemplateConfig().restTemplate()

    private val networkRequestInterface: NetworkRequestInterface =
        retrofit.create(NetworkRequestInterface::class.java)

    fun createNotification(type: String, dbNotification: DbNotification) {

        CoroutineScope(Dispatchers.IO).launch { sendNotification(type, dbNotification) }

    }
    private suspend fun sendNotification(type: String, dbNotification: DbNotification){

        coroutineScope {
            launch(Dispatchers.IO){
                val notificationUrl = appProperties.notificationUrl
                val headers = HttpHeaders()
                headers.contentType = MediaType.APPLICATION_JSON
                val url = when (type) {
                    "CREATE_NOTIFICATION" -> {
                        "/service/notification/create"
                    }
                    else -> {
                        ""
                    }
                }


                val results = restTemplate.postForObject(
                    "$notificationUrl$url", dbNotification, Any::class.java
                )
                println("------ $results")
            }
        }

    }

    fun getPatientDetails(patientPin: String, firstName: String, lastName: String) = runBlocking {
        getPatientData(
            patientPin,
            firstName,
            lastName)
    }
    private suspend fun getPatientData(
        patientPin: String,
        firstName: String,
        lastName: String): Results{

        //This will return the patient uuid

        var details: Any = ""
        var code: Int

        try {
            val retrofitCall = networkRequestInterface?.getUserDetails(patientPin, "default")
            if (retrofitCall != null){
                println("******* $retrofitCall")

                if (retrofitCall.isSuccessful){

                    code = 200
                    val resultBody = retrofitCall.body()

                    if (resultBody != null) {
                        val resultsList = resultBody.results
                        if (resultsList.isNotEmpty()) {

                            //Get Name

                            resultsList.forEach {

                                val name = it.person.display
                                details = it.person.uuid

                            }

                        } else {
                            code = 404
                            details = "No patient found with the provided PIN."
                        }

                    } else{
                        code = 404
                        details = "No patient found with the provided PIN."
                    }

                }else{

                    val errorCode = retrofitCall.code()
                    if (errorCode == 500){
                        code = 404
                        details = "There was an issue connecting. Please try again after sometime."
                    }else{
                        code = 400
                        details = "There is an issue processing the request."
                    }

                }

            }else{
                code = 400
                details = "The requested resource could not be found."
            }
        }catch (e: Exception){

            e.printStackTrace()

            code = 400
            details = "We could not process your request at the moment. Please try again after sometime."

        }

        return Results(code, details)

    }

    fun searchPatientByNationalPassport(nationalPassportNo: String) = runBlocking {
        searchPatientByNationalPassportData(nationalPassportNo)
    }

    private suspend fun searchPatientByNationalPassportData(nationalPassportNo: String): Results {
        var details: Any = ""
        var code: Int

        try {
            val retrofitCall = networkRequestInterface?.getPatientDetailsFull(nationalPassportNo, "full")
            if (retrofitCall != null) {
                if (retrofitCall.isSuccessful) {
                    code = 200
                    val resultBody = retrofitCall.body()

                    if (resultBody != null) {
                        val resultsList = resultBody.results
                        if (!resultsList.isNullOrEmpty()) {
                            // Get the first patient from results
                            val patient = resultsList[0]
                            val patientUuid = patient.uuid

                            // Extract OpenMRS ID (PIN) from identifiers
                            var openMrsId: String? = null
                            patient.identifiers?.forEach { identifier ->
                                if (identifier.identifierType?.display == "OpenMRS ID") {
                                    openMrsId = identifier.identifier
                                }
                            }

                            // Extract phone number from person attributes
                            var phoneNumber: String? = null
                            patient.person?.attributes?.forEach { attribute ->
                                if (attribute.attributeType?.display == "Telephone Number") {
                                    // Handle both string and object values
                                    phoneNumber = when (attribute.value) {
                                        is String -> attribute.value as String
                                        is Map<*, *> -> {
                                            // If it's an object, try to get the display or value field
                                            (attribute.value as Map<*, *>)["display"] as? String
                                                ?: (attribute.value as Map<*, *>)["value"] as? String
                                                ?: attribute.value.toString()
                                        }
                                        else -> attribute.value.toString()
                                    }
                                }
                            }

                            val finalOpenMrsId = openMrsId
                            if (finalOpenMrsId != null) {
                                details = DbOpenMrsPatientSearchResult(
                                    openMrsUuid = patientUuid ?: "",
                                    openMrsId = finalOpenMrsId,
                                    phoneNumber = phoneNumber
                                )
                            } else {
                                code = 404
                                details = "Patient found but OpenMRS ID could not be extracted."
                            }
                        } else {
                            code = 404
                            details = "No patient found with the provided national passport number."
                        }
                    } else {
                        code = 404
                        details = "No patient found with the provided national passport number."
                    }
                } else {
                    val errorCode = retrofitCall.code()
                    var errorBody: String? = null
                    try {
                        errorBody = retrofitCall.errorBody()?.string()
                    } catch (e: Exception) {
                        // Error body already consumed or not available
                    }
                    println("OpenMRS API call failed. Status code: $errorCode, Error body: $errorBody, National ID: $nationalPassportNo")
                    if (errorCode == 500) {
                        code = 404
                        details = "There was an issue connecting to OpenMRS (HTTP $errorCode). ${if (errorBody != null) "Error: $errorBody" else "Please try again after sometime."}"
                    } else {
                        code = 400
                        details = "OpenMRS API returned error code $errorCode. ${if (errorBody != null) "Error: $errorBody" else "No patient found with National ID: $nationalPassportNo"}"
                    }
                }
            } else {
                code = 400
                details = "The requested resource could not be found."
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println("Exception in searchPatientByNationalPassport for National ID: $nationalPassportNo. Error: ${e.message}")
            code = 400
            details = "We could not process your request at the moment. Error: ${e.message ?: e.javaClass.simpleName}. Please try again after sometime."
        }

        return Results(code, details)
    }

    fun getPatientConditionDetails(openMrsId: String) = runBlocking { getPatientConditions(openMrsId) }
    private suspend fun getPatientConditions(openMrsId: String): Results{

        var details: Any
        var code: Int

        try {
            val retrofitCall = networkRequestInterface?.getConditions(openMrsId)
            if (retrofitCall != null){

                if (retrofitCall.isSuccessful){

                    code = 200
                    val resultBody = retrofitCall.body()

                    if (resultBody != null){

                        val dbConditionDataResultsList = ArrayList<DbConditionDataResults>()

                        resultBody.forEach {dbCondition ->

                            val conditionsList = dbCondition.conditions
                            conditionsList.forEach {

                                val id = it.uuid
                                val conditionName = it.concept?.name
                                val status = it.status
                                val additionalDetail = it.additionalDetail
                                val dateCreated = it.dateCreated

                                var convertedDate = ""

                                //Convert milliseconds to date

                                if (dateCreated != null){
                                    val timestamp = dateCreated.toLong()
                                    val instant = Instant.ofEpochMilli(timestamp)
                                    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
                                    convertedDate = instant.atZone(ZoneId.systemDefault()).format(formatter)
                                }

                                val dbConditionDetails = DbConditionDataResults(conditionName, status, convertedDate, additionalDetail)
                                dbConditionDataResultsList.add(dbConditionDetails)

                            }

                        }

                        val dbResultsData = DbResultsData(dbConditionDataResultsList.size, dbConditionDataResultsList)
                        code = 200
                        details = dbResultsData


                    }else{
                        code = 400
                        details = "No data found"
                    }


                }else{

                    val errorCode = retrofitCall.code()
                    if (errorCode == 500){
                        code = 404
                        details = "There was an issue connecting. Please try again after sometime."
                    }else{
                        code = 400
                        details = "There is an issue processing the request."
                    }

                }
                println("------ $code")

            }else{
                code = 400
                details = "The requested resource could not be found."
            }
        }catch (e: Exception){

            e.printStackTrace()

            code = 400
            details = "We could not process your request at the moment. Please try again after sometime."

        }

        return Results(code, details)

    }

    fun getConditionsValuesDetails(openMrsId: String) = runBlocking { getConditionsValues(openMrsId) }

    suspend fun getConditionsValues(patientUuid: String): List<PatientCondition> {

        val response = networkRequestInterface.getConditionsValues(patientUuid)

        if (!response.isSuccessful) {
            throw RuntimeException("Failed to fetch conditions: ${response.code()}")
        }

        val bundle = response.body() ?: return emptyList()

        return bundle.entry
            ?.mapNotNull { it.resource }
            ?.map { resource ->

                val conditionValue = resource.extension
                    ?.firstOrNull {
                        it.url?.contains("non-coded-condition") == true
                    }
                    ?.valueString
                    ?: "Unspecified"

                val statusCode = resource.clinicalStatus
                    ?.coding
                    ?.firstOrNull()
                    ?.code
                    ?: "unknown"

                val recorder = resource.recorder?.display ?: "system"

                val recordedDate = resource.recordedDate
                    ?.let { OffsetDateTime.parse(it) }
                    ?: OffsetDateTime.now()

                PatientCondition(
                    condition = conditionValue,
                    clinicalStatus = statusCode,
                    recordedBy = recorder,
                    recordedDate = recordedDate
                )
            }
            ?: emptyList()
    }

    //Get allergy
    fun getPatientAllergyDetails(openMrsId: String) = runBlocking { getPatientAllergy(openMrsId) }
    private suspend fun getPatientAllergy(openMrsId: String): Results{

        var details: Any
        var code: Int

        try {
            val retrofitCall = networkRequestInterface.getAllergy(openMrsId)

            if (retrofitCall.isSuccessful){

                val resultBody = retrofitCall.body()
                if (resultBody != null){

                    val dbAllergyDataResultsList = ArrayList<DbAllergyDataResults>()
                    val bodyResult = resultBody.results

                    if (!bodyResult.isNullOrEmpty()){

                        for (dbAllergyData in bodyResult) {

                            val display = dbAllergyData.display
                            val severity = dbAllergyData.severity ?: ""

                            val reactionList = ArrayList<String>()

                            val reactionsList = dbAllergyData.reactions
                            val comment = dbAllergyData.comment

                            reactionsList?.forEach{

                                val reaction = it.reaction?.display ?: ""
                                reactionList.add(reaction)
                            }
                            reactionList.add(comment?: "")
                            val dbAllergyDataResults = DbAllergyDataResults(display ?: "", reactionList, severity, "")
                            dbAllergyDataResultsList.add(dbAllergyDataResults)
                            // Remove duplicate reactions
                            dbAllergyDataResultsList.distinct()
                        }

                    }

                    val dbResultsData = DbResultsData(dbAllergyDataResultsList.size, dbAllergyDataResultsList)


                    code = 200
                    details = dbResultsData

                }else{
                    code = 400
                    details = "No allergy data found"
                }


            }else{

                val errorCode = retrofitCall.code()
                println("------1 $errorCode")

                if (errorCode == 500){
                    code = 400
                    details = "We could not find any allergies."
                }else{
                    code = 400
                    details = "There is an issue processing the request."
                }

            }
        }catch (e: Exception){

            e.printStackTrace()

            code = 400
            details = "We could not process your request at the moment. Please try again after sometime."

        }

        return Results(code, details)

    }

    //Get drug
    fun getPatientDrugDetails(openMrsId: String) = runBlocking { getPatientDrugs(openMrsId) }
    private suspend fun getPatientDrugs(openMrsId: String): Results{

        var details: Any
        var code: Int

        try {
            val retrofitCall = networkRequestInterface?.getDrugs(openMrsId)
            if (retrofitCall != null){

                if (retrofitCall.isSuccessful){

                    code = 200
                    val resultBody = retrofitCall.body()
                    if (resultBody != null){

                        val bodyResult = resultBody.results

                        val dbResultsData =  DbResultsData(bodyResult.size, bodyResult)

                        code = 200
                        details = dbResultsData

                    }else{
                        code = 400
                        details = "No allergy data found"
                    }


                }else{

                    val errorCode = retrofitCall.code()
                    if (errorCode == 500){
                        code = 404
                        details = "There was an issue connecting. Please try again after sometime."
                    }else{
                        code = 400
                        details = "There is an issue processing the request."
                    }

                }
                println("------ $code")

            }else{
                code = 400
                details = "The requested resource could not be found."
            }
        }catch (e: Exception){

            e.printStackTrace()

            code = 400
            details = "We could not process your request at the moment. Please try again after sometime."

        }

        return Results(code, details)

    }

    //Get drug details
    fun getPatientDrugDetailsData(openMrsId: String, drugId: String) = runBlocking { getPatientDrugsData(openMrsId, drugId) }
    private suspend fun getPatientDrugsData(openMrsId: String, drugId: String): Results{

        var details: Any
        var code: Int

        try {
            val retrofitCall = networkRequestInterface?.getDrugsDetails(drugId)
            if (retrofitCall != null){

                if (retrofitCall.isSuccessful){

                    code = 200
                    val resultBody = retrofitCall.body()
                    if (resultBody != null) {

                        code = 200
                        details = resultBody

                    }else{
                        code = 400
                        details = "No allergy data found"
                    }


                }else{

                    val errorCode = retrofitCall.code()
                    if (errorCode == 500){
                        code = 404
                        details = "There was an issue connecting. Please try again after sometime."
                    }else{
                        code = 400
                        details = "There is an issue processing the request."
                    }

                }
                println("------ $code")

            }else{
                code = 400
                details = "The requested resource could not be found."
            }
        }catch (e: Exception){

            e.printStackTrace()

            code = 400
            details = "We could not process your request at the moment. Please try again after sometime."

        }

        return Results(code, details)

    }

    //Get vitals
    fun getPatientVitalsDetails(openMrsId: String) = runBlocking { getPatientVitals(openMrsId) }
    suspend fun getPatientVitals(openMrsId: String): Results {
        return try {
            val response = networkRequestInterface.getVitals(openMrsId, "full")

            if (!response.isSuccessful) {
                val msg = if (response.code() == 500) "We could not find any vitals." else "There is an issue processing the request."
                Results(400, msg)
            } else {
                val body = response.body()
                if (body == null || body.results.isEmpty()) {
                    Results(400, "No vitals data found")
                } else {
                    val vitalDTOs = body.results.flatMap { result ->
                        result.encounter?.obs.orEmpty().mapNotNull { obs ->
                            obs.display?.let { display ->
                                val (conceptName, value, unit) = parseConcept(display)
                                val dateRecorded = formatDate(result.obsDatetime ?: result.encounter?.encounterDatetime)
                                val encounterType = result.encounter?.encounterType?.display ?: ""
                                val encounterProvider = result.encounter?.encounterProviders?.firstOrNull()?.display
                                val location = result.location?.display
                                PatientVitalDTO(conceptName, value, unit, dateRecorded, encounterType, encounterProvider, location)
                            }
                        }
                    }
                    Results(200, vitalDTOs)
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            Results(400, "We could not process your request at the moment. Please try again after sometime.")
        }
    }

    private fun parseConcept(display: String): Triple<String, String, String> {
        // Example: "Weight (kg): 69.0"
        val parts = display.split(":").map { it.trim() }
        val conceptNameWithUnit = parts.getOrNull(0) ?: ""
        val value = parts.getOrNull(1) ?: ""

        val unitRegex = Regex("\\(([^)]+)\\)")
        val unit = unitRegex.find(conceptNameWithUnit)?.groupValues?.get(1) ?: ""
        val conceptName = conceptNameWithUnit.replace(unitRegex, "").trim()

        return Triple(conceptName, value, unit)
    }

    private fun formatDate(dateStr: String?): String {
        if (dateStr.isNullOrEmpty()) return ""
        return try {
            val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            val odt = OffsetDateTime.parse(dateStr, formatter)
            odt.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
        } catch (e: Exception) {
            dateStr
        }
    }



    fun getPatientVisits(openMrsId: String) = runBlocking { fetchPatientVisitsBac(openMrsId) }
    private suspend fun fetchPatientVisitsBac(openMrsId: String): Results {
        var details: Any
        var code: Int

        try {
            val retrofitCall = networkRequestInterface.getVisits(openMrsId, "full")

            if (retrofitCall.isSuccessful) {
                val resultBody = retrofitCall.body()

                if (resultBody != null) {
                    val visitsRaw = resultBody.results
                    val visitSummaries = visitsRaw?.map { mapVisitSummary(it) }
                    if (visitSummaries != null) {

                        val dbResultsData = DbResultsData(visitSummaries.size, visitSummaries)
                        code = 200
                        details = dbResultsData

                    }else {
                        code = 400
                        details = "No visits found"
                    }
                } else {
                    code = 400
                    details = "No visits found"
                }
            } else {
                val errorCode = retrofitCall.code()
                code = if (errorCode == 500) {
                    404
                } else {
                    400
                }
                details = "There is an issue processing the request."
            }
        } catch (e: Exception) {
            e.printStackTrace()
            code = 400
            details = "We could not process your request at the moment. Please try again after sometime."
        }

        return Results(code, details)
    }

//    fun getVisitById(visitId: String) = runBlocking { fetchVisitById(visitId) }
//    private suspend fun fetchVisitById(visitId: String): Results {
//        var details: Any
//        var code: Int
//
//        try {
//            val retrofitCall = networkRequestInterface?.getVisitById(visitId, "full")
//            if (retrofitCall != null) {
//
//                if (retrofitCall.isSuccessful) {
//                    val resultBody = retrofitCall.body()
//                    if (resultBody != null) {
//                        val visitSummary = mapVisitSummary(resultBody)
//                        code = 200
//                        details = visitSummary
//                    } else {
//                        code = 400
//                        details = "No visit found"
//                    }
//                } else {
//                    val errorCode = retrofitCall.code()
//                    code = if (errorCode == 404) {
//                        404
//                    } else {
//                        400
//                    }
//                    details = "There is an issue processing the request."
//                }
//            } else {
//                code = 400
//                details = "The requested resource could not be found."
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//            code = 400
//            details = "We could not process your request at the moment. Please try again after sometime."
//        }
//
//        return Results(code, details)
//    }

    private fun mapVisitSummary(visit: VisitResponse): VisitSummary {
//        val encounterSummaries = visit.encounters?.map { encounter ->
//            VisitEncounterSummary(
//                uuid = encounter.uuid,
//                display = encounter.display,
//                encounterType = encounter.encounterType?.display,
//                encounterDatetime = encounter.encounterDatetime,
//                location = encounter.location?.display
//            )
//        } ?: emptyList()

        return VisitSummary(
            uuid = visit.uuid,
            visitType = visit.visitType?.display,
            startDatetime = visit.startDatetime,
            stopDatetime = visit.stopDatetime,
            location = visit.location?.display,
            encounters = emptyList()
        )
    }

}