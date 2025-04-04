package com.intellisoft.botswanaemrauthentication

import com.fasterxml.jackson.annotation.JsonProperty
import com.intellisoft.botswanaemrauthentication.authentication.entity.PatientDetails
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
    val nationalPassportNo: String ,
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
    val profileUrl: String?

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
data class DbDrugs(
    val results : List<DbDrugsResults>
)
data class DbDrugsResults(
    val uuid: String,
    val display: String,
    val type: String?
)
data class DbAllergyResults(
    val results: List<DbAllergyData>
)
data class DbAllergyData(
    val display: String,
    val uuid: String,
    val reactions: List<DbAllergyReactions>
)
data class DbAllergyReactions(
    val reaction: DbAllergyReactionsData
)
data class DbAllergyReactionsData(
    val uuid: String,
    val display: String
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
    val reactions: List<String>
)
data class DbConditionDataResults(
    val name: String?,
    val status: String?,
    val dateCreated: String?,
    val additionalDetail: String?
)

data class DbMedicalHistory(
    var allergys: Any? = emptyList<DbAllergyDataResults>(),
    var conditions: Any? = emptyList<DbConditionDataResults>(),
    var drugs : Any? = emptyList<DbDrugs>()
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
}