package com.intellisoft.botswanaemrauthentication

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.intellisoft.botswanaemrauthentication.authentication.entity.PatientDetails
import java.time.OffsetDateTime
import java.util.ListResourceBundle
import java.util.Scanner
import java.util.UUID

data class DbAppValues(
    val username: String,
    val password: String,
    val openMrsUrl: String
)
data class KeycloakUserId(
    val userId: String
)
data class ResetPasswordRequest(
    val emailAddress: String ,
    val password: String,
    val confirmPassword: String,
    val otpCode: String)

data class DbEmailAddress(
    val subject: String,
    val greeting: String,
    val message: String,

)

data class ResendLink(
    val emailAddress: String = ""
)

data class DbVerificationLink(
    val baseUrl : String,
    val verificationLink: String,
    val patientDetails: PatientDetails
)

data class Results(
    val code: Int,
    val message: Any)

data class LoginResponse(
    val access_token: String,
    val expires_in: Long,
    val refresh_token: String,
    val refresh_expires_in: Long,
    val token_type: String,
    val roles: List<String>)
data class LoginRequest(
    val emailAddress: String ,
    val password: String 
)
data class RegisterRequest(
    val password: String,
    val confirmPassword: String,
    val emailAddress: String ,
    val firstName:String ,
    val lastName: String ,
    val phoneNumber: String ,
    val dateOfBirth: String ,
    val gender: String ,
    var patientIdentificationNo: String? ,
    val nationalPassportNo: String? ,
    val username: String ,
    val identificationType: String,
    val imageUrl: String,
    )
data class UpdateUserDetails(
    val firstName:String? ,
    val lastName: String? ,
    val phoneNumber: String? ,
    val patientIdentificationNo: String? ,
    val username: String?,

    val imageUrl: String?,
    val gender: String?,
    val dateOfBirth: String?



)
data class LinkPatientRequest(
    val phoneNumber: String,
    val nationalPassportNo: String
)

data class DbPerson(
    val names:List<DbName>,
    val gender: String?,
    val birthdate:String?,
    val addresses:List<DbAddress>
)
data class DbName(
    val givenName: String,
    val familyName:String
)
data class DbAddress(
    val address1:String,
    val cityVillage:String,
    val country: String,
    val postalCode: String
)
data class DbPersonSuccess(
    val uuid:String
)

data class DbPatient(
    val person: String?,
    val identifiers: List<DbIdentifiers>
)
data class DbIdentifiers(
    val identifier: String,
    val identifierType: String,
    val location: String,
    val preferred: Boolean
)
data class DbNotification(
    val title: String,
    val message: String,
    val userId: String,
    val notificationType: String,
    val process: String
)
data class DbPatientData(
    val patientType: String,
    val idType: String,
    val idNumber: String,
    val givenName: String,
    val middleName: String,
    val familyName: String,
    val gender: String,
    val dob: String,
    val email: String,
    val contactNumber: String,
    val occupation : String,
    val employerName: String,
    val homeaddress: String,
    val patientFacilityLocation: String,
    val nok: List<DbKinNext>,
    val relationship: List<DbRelationship>
)
data class DbKinNext(
    val idType : String,
    val nokIdNumber : String,
    val nokFullName : String,
    val nokRelationship : String,
    val nokContact : String,
    val nokEmail : String,
)
data class DbRelationship(
    val patientId: String,
    val relationship: String
)
enum class NotificationDetails(){
    SYSTEM,
    AUTHENTICATION,
    PATIENT_ACTIVATION,
    CONSENT,

}
data class DbResults(
    val message: String
)
data class LinkPatientResponse(
    val message: String,
    val patient: DbPatientDetails,
    val alreadyLinked: Boolean = false
)
data class DbPatientDetails(

    val id: String,
    val emailAddress: String,
    val firstName:String,
    val lastName:String,
    val phoneNumber: String,
    val dateOfBirth: String,
    val patientIdentificationNo: String?,
    val openMrsId: String?,
//    val keycloakId: String,
    val gender: String,
    val username: String,
    val patient: Boolean,
    val profileUrl: String?,
    val nationalPassportNo: String?

)

data class DbDrugsDetails(
    val uuid: String,
    val orderNumber: String?,
    val careSetting: CareSetting?,
    val dateActivated: String?,
    val encounter: Encounter?,
    val orderer: Orderer?,
    val urgency: String?,
    val instructions: String?,
    val display: String?
)
data class Orderer(
    val display: String
)
data class Encounter(
    val display: String
)
data class CareSetting(
    val display: String
)
@JsonIgnoreProperties(ignoreUnknown = true)
data class DbDrugs(
    val results : List<DbDrugsResults>? = null
)
@JsonIgnoreProperties(ignoreUnknown = true)
data class DbDrugsResults(
    val uuid: String? = null,
    val display: String? = null,
    val type: String? = null
)
@JsonIgnoreProperties(ignoreUnknown = true)
data class DbAllergyResults(
    val results: List<DbAllergyData>? = null
)
@JsonIgnoreProperties(ignoreUnknown = true)
data class DbAllergyData(
    val display: String? = null,
    val comment: String? = null,
    val severity: String? = null,
    val uuid: String? = null,
    val reactions: List<DbAllergyReactions>? = null
)
@JsonIgnoreProperties(ignoreUnknown = true)
data class DbAllergyReactions(
    val reaction: DbAllergyReactionsData? = null
)
@JsonIgnoreProperties(ignoreUnknown = true)
data class DbAllergyReactionsData(
    val uuid: String? = null,
    val display: String? = null
)

data class DbConditionsData(
    val conditions : List<DbCondition>
)
data class DbCondition(
    val uuid: String?,
    val concept: DBConcept?,
    val status: String?,
    val additionalDetail: String?,
    val dateCreated: String?
)
data class DBConcept(
    val uuid: String,
    val name: String
)
data class DbConditionDetails(
    val id: String,
    val conditionName: String,
    val status: String,
    val additionalDetail: String,
)



data class DbOpenMrsResult(
    val results: List<DbOpenMrsInfo>
)
data class DbOpenMrsInfo(
    val person: DbOpenMrsData
)
data class DbOpenMrsData(
    val uuid: String,
    val display: String,
    val gender: String,
    val age: String,
    val birthdate: String,
    val birthdateEstimated: String,
    val dead: String,
    val deathDate: String,
    val causeOfDeath: String,
    val birthtime: String,
    val deathdateEstimated: String,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class DbOpenMrsPatientResult(
    val results: List<DbOpenMrsPatientInfo>? = null
)
@JsonIgnoreProperties(ignoreUnknown = true)
data class DbOpenMrsPatientInfo(
    val uuid: String? = null,
    val identifiers: List<DbOpenMrsIdentifier>? = null,
    val person: DbOpenMrsPersonData? = null
)
@JsonIgnoreProperties(ignoreUnknown = true)
data class DbOpenMrsIdentifier(
    val identifier: String? = null,
    val identifierType: DbOpenMrsIdentifierType? = null,
    val preferred: Boolean = true
)
@JsonIgnoreProperties(ignoreUnknown = true)
data class DbOpenMrsIdentifierType(
    val display: String? = null
)
@JsonIgnoreProperties(ignoreUnknown = true)
data class DbOpenMrsPersonData(
    val uuid: String? = null,
    val attributes: List<DbOpenMrsAttribute>? = null
)
@JsonIgnoreProperties(ignoreUnknown = true)
data class DbOpenMrsAttribute(
    val value: Any? = null, // Can be String or Object (for complex attributes like Education, Employment Sector)
    val attributeType: DbOpenMrsAttributeType ? = null
)
@JsonIgnoreProperties(ignoreUnknown = true)
data class DbOpenMrsAttributeType(
    val display: String? = null
)
data class DbOpenMrsPatientSearchResult(
    val openMrsUuid: String,
    val openMrsId: String, // The OpenMRS ID (PIN)
    val phoneNumber: String?
)
data class DbOpenMrsLocal(
    val personDetails : Any,
    val patientDetails: Any?
)
data class DbUserRole(
    val keyCloakId: String,
    val roleName: String
)
data class DbConsent(
    val language: String,
    val plannedOperation: String,
    val patientName: String,
    val operationNature: String,
    val patientRelationship: String,
    val signedBy: String,
    val witnessBy: String,
)

data class DbRefreshToken(
    @JsonProperty("refreshToken")
    val refreshToken: String
)
data class DbRefreshTokenResponse(
    @JsonProperty("access_token")
    val accessToken: String,
    @JsonProperty("expires_in")
    val expiresIn: Int,
    @JsonProperty("refresh_expires_in")
    val refreshExpiresIn: Int,
    @JsonProperty("refresh_token")
    val refreshToken: String,
    @JsonProperty("token_type")
    val tokenType: String,

)

data class DbResultsData(
    val count: Int,
    val results: List<Any>
)

data class DbAllergyDataResults(
    val name: String,
    val reactions: List<String>,
    val severity: String,
    val dateIdentified: String,

)
data class DbConditionDataResults(
    val name: String?,
    val status: String?,
    val dateCreated: String?,
    val additionalDetail: String?
)

data class DbVitalsResults(
    val results: List<DbVitalObservation>
)

data class DbVitalObservation(
    val uuid: String?,
    val display: String?,
    val concept: DbVitalConcept?,
    val obsDatetime: String?,
    val value: Any?,
    val encounter: DbVitalEncounter?,
    val location: DbVitalLocation?
)

data class DbVitalConcept(
    val uuid: String?,
    val display: String?
)

data class DbVitalEncounter(
    val uuid: String?,
    val display: String?
)

data class DbVitalLocation(
    val uuid: String?,
    val display: String?
)

//data class DbVisitRaw(
//    val uuid: String?,
//    val display: String?,
//    val startDatetime: String?,
//    val stopDatetime: String?,
//    val location: DbVitalLocation?,
//    val visitType: DbVisitType?,
//    val encounters: List<DbEncounterRaw>?
//)

data class DbVisitType(
    val uuid: String?,
    val display: String?
)

data class DbEncounterRaw(
    val uuid: String?,
    val display: String?,
    val encounterDatetime: String?,
    val encounterType: DbEncounterType?,
    val location: DbVitalLocation?
)

data class DbEncounterType(
    val uuid: String?,
    val display: String?
)

data class VisitEncounterSummary(
    val uuid: String?,
    val display: String?,
    val encounterType: String?,
    val encounterDatetime: String?,
    val location: String?
)

data class VisitSummary(
    val uuid: String?,
    val visitType: String?,
    val startDatetime: String?,
    val stopDatetime: String?,
    val location: String?,
    val encounters: List<VisitEncounterSummary>
)

data class DbMedicalHistory(
    var allergys: Any? = emptyList<DbAllergyDataResults>(),
    var conditions: Any? = emptyList<DbConditionDataResults>(),
    var drugs : Any? = emptyList<DbDrugs>(),
    var vitals: Any? = emptyList<DbVitalDataResults>()
){
    fun setAllergy(allergy: List<Any>){
        this.allergys = allergy
    }
    fun setCondition(condition: List<Any>){
        this.conditions = condition
    }
    fun setDrug(drug: List<Any>){
        this.drugs = drug
    }
    fun setVitals(vitals: List<Any>){
        this.vitals = vitals
    }
}
//New Conditions data class
data class PatientCondition(
    val condition: String,
    val clinicalStatus: String,
    val recordedBy: String,
    val recordedDate: OffsetDateTime
)

data class PatientConditionsResponse(
    val patientId: String,
    val conditions: List<PatientCondition>
)
//data class FhirConditionBundle(
//    @JsonProperty("entry"  )
//    val entry: List<Entry>? = null
//) {
//    data class Entry(
//        @JsonProperty("resource"  )
//        val resource: ConditionResource? = null
//    )
//}
@JsonIgnoreProperties(ignoreUnknown = true)
data class FhirConditionBundle(
    val entry: List<Entry>? = null
)
@JsonIgnoreProperties(ignoreUnknown = true)
data class Entry(
    val resource: ConditionResource? = null
)
@JsonIgnoreProperties(ignoreUnknown = true)
data class ConditionResource(
    val extension: List<Extension>? = null,
    val clinicalStatus: ClinicalStatus? = null,
    val recorder: Recorder? = null,
    val recordedDate: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Extension(
    val url: String? = null,
    val valueString: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ClinicalStatus(
    val coding: List<Coding>? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Coding(
    val code: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Recorder(
    val display: String? = null
)


@JsonIgnoreProperties(ignoreUnknown = true)
data class DbVisitResponse(
    @JsonProperty("results")
    val results: List<VisitResponse>? = null
)
@JsonIgnoreProperties(ignoreUnknown = true)
data class VisitResponse(

    @JsonProperty("uuid")
    val uuid: String? = null,

    @JsonProperty("display")
    val display: String? = null,

    @JsonProperty("visitType")
    val visitType: VisitType? = null,

    @JsonProperty("location")
    val location: Location? = null,

    @JsonProperty("startDatetime")
    val startDatetime: String? = null,

    @JsonProperty("stopDatetime")
    val stopDatetime: String? = null,

    @JsonProperty("auditInfo")
    val auditInfo: AuditInfo? = null
) {

    /**
     * Controlled final compiled output.
     * This ensures downstream layers do not depend on nested structures.
     */
    fun toSimpleVisit(): SimpleVisit {
        return SimpleVisit(
            uuid = uuid,
            display = display,
            visitTypeDisplay = visitType?.display,
            locationDisplay = location?.display,
            startDatetime = startDatetime,
            stopDatetime = stopDatetime,
            auditorDisplay = auditInfo?.creator?.display
        )
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class VisitType(
    @JsonProperty("display")
    val display: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Location(
    @JsonProperty("display")
    val display: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AuditInfo(
    @JsonProperty("creator")
    val creator: Creator? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Creator(
    @JsonProperty("display")
    val display: String? = null
)

/**
 * Final flattened structure for UI / Domain consumption.
 */
data class SimpleVisit(
    val uuid: String?,
    val display: String?,
    val visitTypeDisplay: String?,
    val locationDisplay: String?,
    val startDatetime: String?,
    val stopDatetime: String?,
    val auditorDisplay: String?
)

//Vitals
@JsonIgnoreProperties(ignoreUnknown = true)
data class VitalsResponse(
    val results: List<VitalObservationResult> = emptyList()
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class VitalObservationResult(
    val uuid: String? = null,
    val display: String? = null,
    val obsDatetime: String? = null,
    val location: VitalLocation? = null,
    val encounter: VitalEncounter? = null,
    val status: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class VitalLocation(
    val display: String? = null,
    val name: String? = null,
    val country: String? = null,
    val countyDistrict: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class VitalEncounter(
    val encounterDatetime: String? = null,
    val encounterType: EncounterType? = null,
    val obs: List<Obs>? = null,
    val encounterProviders: List<EncounterProvider>? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EncounterType(
    val display: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Obs(
    val display: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EncounterProvider(
    val display: String? = null
)

data class PatientVitalDTO(
    val conceptName: String,
    val value: String,
    val unit: String,
    val dateRecorded: String,
    val encounterType: String,
    val encounterProvider: String?,
    val location: String?
)
data class DbVitalDataResults(
    val conceptName: String?,
    val value: String?,
    val unit: String?,
    val dateRecorded: String?,
    val encounter: String?
)
data class DbMedicalHistoryData(
    val allergies : List<DbAllergyDataResults> = emptyList(),
    val conditions: List<PatientCondition>     = emptyList(),
    val drugs     : List<DbDrugsResults>       = emptyList(),
    val vitals    : List<PatientVitalDTO>      = emptyList(),
    val visits    : List<VisitSummary>         = emptyList()
)