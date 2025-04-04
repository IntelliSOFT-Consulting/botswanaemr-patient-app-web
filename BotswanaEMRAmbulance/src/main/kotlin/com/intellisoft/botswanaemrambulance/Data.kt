package com.intellisoft.botswanaemrambulance


import com.fasterxml.jackson.annotation.JsonProperty

data class Results(
    val code: Int,
    val message: Any)


data class DbNotification(
    val title: String,
    val message: String,
    val userId: String,
    val notificationType: String,
    val process: String
)
data class DbDataResults(
    val count:Int,
    val next:String? = null,
    val previous:String? = null,
    val details: Any
)
data class DbResults(
    val details: Any
)
data class PatientDetails(

    @JsonProperty("id")
    val id: String,
    @JsonProperty("phoneNumber")
    val phoneNumber: String,
    @JsonProperty("gender")
    val gender: String,
    @JsonProperty("emailAddress")
    val emailAddress: String,
    @JsonProperty("username")
    val username: String,
    @JsonProperty("openMrsId")
    val openMrsId: String?
)

data class DbIncidentData(
    val name: String,
    val location: String,
    val latitude: Double,
    val longitude: Double,
    val idNumber: String,
    val incidentDetails: String,
    val additionalInformation: String
)
data class DbAmbulance(
    val name: String?,
    val registrationNumber:String,
    val status: String? = null
)
enum class AmbulanceIncidentStatus(){
    AVAILABLE, DISPATCHED, OFFLINE
}
enum class IncidentStatus(){
    AWAITING, ON_ROUTE, IN_HOSPITAL, REJECTED
}
enum class DriverStatus(){
    UNASSIGNED, ASSIGNED
}
data class DbIncidentRequest(
    val userName: String,
    val location: String,
    val incident: String,
    val phoneNumber: String? = null,
    val additionalInformation: String? = null)

data class DbUpdateIncidentRequest(
    val additionalInformation: String,
    val incidentStatus: String?
)
data class DbDriverData(
    val id: String,
    val username: String,
    val emailAddress: String,
    val hasAmbulance: String? = null
)
data class DbAcceptRequest(
    val ambulanceRegNo: String,
    val incidentId: String,
    val pickUpLocation: String,
    val dropOffLocation: String,
)
data class DbRejectRequest(
    val incidentId: String,
    val reason: String
)
data class DbUserRole(
    val keyCloakId: String,
    val roleName: String
)
enum class NotificationDetails(){
    SYSTEM,
    AUTHENTICATION,
    PATIENT_ACTIVATION,
    CONSENT,
    PATIENT_UPDATE,

    DRIVER_UPDATE

}