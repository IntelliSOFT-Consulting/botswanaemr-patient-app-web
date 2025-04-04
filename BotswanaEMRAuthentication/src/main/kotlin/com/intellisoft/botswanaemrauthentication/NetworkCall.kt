package com.intellisoft.botswanaemrauthentication

import kotlinx.coroutines.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import retrofit2.Retrofit
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.annotation.PostConstruct


@Component
class NetworkCall() {

    @Autowired
    lateinit var appProperties: AppProperties

    var restTemplate = RestTemplateConfig().restTemplate()

    @Autowired
    private val retrofit: Retrofit? = null

    private var networkRequestInterface: NetworkRequestInterface? = null

    @PostConstruct
    fun setup() {
        networkRequestInterface = retrofit?.create(NetworkRequestInterface::class.java)
    }


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

    //Get allergy
    fun getPatientAllergyDetails(openMrsId: String) = runBlocking { getPatientAllergy(openMrsId) }
    private suspend fun getPatientAllergy(openMrsId: String): Results{

        var details: Any
        var code: Int

        try {
            val retrofitCall = networkRequestInterface?.getAllergy(openMrsId)

            if (retrofitCall != null){

                if (retrofitCall.isSuccessful){

                    val resultBody = retrofitCall.body()
                    if (resultBody != null){

                        val dbAllergyDataResultsList = ArrayList<DbAllergyDataResults>()
                        val bodyResult = resultBody.results

                        if (bodyResult.isNotEmpty()){

                            for (dbAllergyData in bodyResult) {

                                val display = dbAllergyData.display

                                val reactionList = ArrayList<String>()

                                val reactionsList = dbAllergyData.reactions
                                reactionsList.forEach{

                                    val reaction = it.reaction.display
                                    reactionList.add(reaction)
                                }
                                val dbAllergyDataResults = DbAllergyDataResults(display, reactionList)
                                dbAllergyDataResultsList.add(dbAllergyDataResults)
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




}