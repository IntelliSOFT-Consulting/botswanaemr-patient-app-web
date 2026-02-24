package com.intellisoft.botswanaemrappointments

import com.fasterxml.jackson.annotation.JsonProperty

data class DbAppValues(
    val username: String,
    val password: String,
    val openMrsUrl: String
)
data class Results(val code: Int, val details: Any?)
data class Error(var timestamp: String, var status: String, var error: String)
data class DbResultsData(val message: String)
data class DbCancelAppointmentResponse(
    val message: String,
    val appointment: DbCancelAppointmentDetails?
)
data class DbCancelAppointmentDetails(
    val uuid: String?,
    val status: String?,
    val display: String?,
    val appointmentType: String?,
    val date: String?,
    val time: String?,
    val provider: String?,
    val location: String?
)

data class DbServiceTypes(
    val results:List<DbAppointmentTypesResults>
)
data class DbAppointmentTypesResults(
    val uuid: String,
    val name: String,
    val duration: Int
)

data class DbLocations(
    val results:List<DbResults>
)
data class DbResults(
    val uuid: String,
    val display: String
)

data class DbProvider(
    val results: List<DbProviderResults>
)
data class DbProviderResults(
    val uuid:String,
    val person: DbPerson?
)
data class DbPerson(
    val uuid: String,
    val display: String
)

data class DbSearchAvailableAppointmentBlock(
    val results: List<DbAvailableAppointmentBlock>
)
data class DbAvailableAppointmentBlock(
    val uuid: String,
    val startDate: String,
    val endDate: String,
    val countOfAppointments: Int,
    val unallocatedMinutes: Int,
    val display: String,
    val appointmentBlock: DbAppointmentBlock

    )
data class DbAppointmentBlock(
    val display: String,
    val startDate: String?,
    val endDate: String?,
    val provider: DbProviderResults?,
    @JsonProperty("location")
    val location: DbLocation?,
    val types :List<DbResults?>)

data class DbAvailableTimeBlock(
    val results: List<DbAppointmentBlock>
)


data class DbPatient(
    val results: List<DbPatientData>
)
data class DbPatientData(
    val uuid: String,
    val person: DbPersonData?

)
data class DbPersonData(
    val gender: String,
    val age: Int,
    val birthdate: String,
    val display: String,
    val dead: Boolean,
    val deathDate: String?,
    val personName: DbPersonName
)
data class DbPersonName(
    val display: String,
    val uuid:String,
)


data class DbSuccessAppointmentBlock(
    val uuid: String?,
    val startDate: String?,
    val endDate: String?,
    val provider: DbProviderDataInfo,
    val location: DbResults,
    val types: List<DbResults>
)
data class DbProviderDataInfo(
    val person: DbPerson
)


data class DbAppointmentBlockResults(
    val uuid: String,
    val startDate: String,
    val endDate: String,
    val provider: DbProviderData,
    val location: DbResults,
    val types: List<Dbtypes>
)
data class Dbtypes(
    val uuid: String,
    val display: String,
    val location: DbLocation?
)

data class DbLocation(
    val uuid: String,
    val display: String
)

data class DbProviderData(
    val person: DbPerson?
)

data class DbCreateAppointment(
    val types: List<String>,
    val location: String?,
    val startDate: String?,
    val endDate: String?,
    val provider: String?
)
data class DbCreateAppointmentBlock(
    val types: List<String>,
    val location: String?,
    val startDate: String?,
    val endDate: String?,
    val provider: String?
)

data class DbScheduleNewAppointment(
    val appointmentType: String?,
    val reason: String?,
    val timeSlot: String?)


data class DbPatientAppointmentData(
    val results: List<DbPatientAppointment>
)

data class DbPatientAppointment(
    val uuid: String,
    val display: String,
    val timeSlot: DbTimeSlot,
    val patient: DbPatientData,
    val status: String,
    val reason:String?,
    val cancelReason:String?,

)
data class DbTimeSlotResult(
    val results : List<DbTimeSlot>
)
data class DbTimeSlot(
    val uuid: String,
    val startDate: String?,
    val endDate: String?,
    val appointmentBlock: DbAppointmentBlock?,
    val countOfAppointments: Int,
    val unallocatedMinutes: Int

)

data class DbSuccessAppointment(
    val uuid: String,
    val display: String,
    val timeSlot: DbTimeSlot,
    val patient: DbPatientData,
    val status: String,
    val reason:String?,
    val cancelReason:String?,
)
data class DbUserData(
    val keycloakId: String?,
    val occupation : String,
    val employerName: String,
    val homeaddress: String,
    val patientFacilityLocation: String,
    val nok: List<DbKinNext>,
    val relationship: List<DbRelationship>
)

data class DbPatientDetails(
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
    PATIENT_APPOINTMENT,
}

enum class AppointmentTypeData(){
    SCHEDULE_APPOINTMENT,
    REQUEST_APPOINTMENT,
}

data class DbNotification(
    val title: String,
    val message: String,
    val userId: String,
    val notificationType: String,
    val process: String
)
data class DbCreatePatientSuccess(
    val status: String,
    val response: Any
)
data class DbPersonDetails(
    val uuid: String,
    val display: String
)
enum class EndpointTypes(){
    CREATE_PATIENT
}
data class UserDetails(

    @JsonProperty("id")
    val id: String,
    @JsonProperty("keycloakId")
    val keycloakId: String?,
    @JsonProperty("phoneNumber")
    val phoneNumber: String,
    @JsonProperty("gender")
    val gender: String,
    @JsonProperty("emailAddress")
    val emailAddress: String,
    @JsonProperty("username")
    val username: String,
    @JsonProperty("openMrsId")
    val openMrsId: String?,
    @JsonProperty("patient")
    val patient: Boolean,
    @JsonProperty("patientIdentificationNo")
    val patientIdentificationNo: String?
)
data class DbFacilities(
    val attributes: DbFacilityAttributes,
    val data : String
)
data class DbFacilityAttributes(
    val name: String,
    val id: String
)
data class DbRequestAppointment(
    val appointmentType: String,
    val notes: String,

    val provider: String?,
)

data class DbScheduleAppointmentSuccess(
    @JsonProperty("results")
    val results: List<DbScheduleAppointment>
)
data class DbScheduleAppointment(
    @JsonProperty("uuid")
    val uuid: String,
    @JsonProperty("display")
    val display: String,
    @JsonProperty("appointmentType")
    val appointmentType: Dbtypes?,
    @JsonProperty("reason")
    val reason: String?,
    @JsonProperty("timeSlot")
    val timeSlot: DbTimeSlot?,
    @JsonProperty("status")
    val status: DbStatus?,
)
data class DbStatus(
    @JsonProperty("name")
    val name: String?
)


data class DbRequestAppointmentSuccess(
    @JsonProperty("results")
    val results: List<DbRequestAppointmentDetails>
)
data class DbRequestAppointmentDetails(
    @JsonProperty("uuid")
    val uuid: String,
    @JsonProperty("display")
    val display: String,
    @JsonProperty("appointmentType")
    val appointmentType: Dbtypes?,
    @JsonProperty("notes")
    val notes: String?,

    @JsonProperty("status")
    val status: String?,
    @JsonProperty("requestedOn")
    val requestedOn: String?,

    @JsonProperty("provider")
    val provider: DbProviderResults?,

    @JsonProperty("location")
    val location: DbLocation?,

    @JsonProperty("requestedBy")
    val requestedBy: DbProviderResults?
)

data class DbMyAppointments(
    val requestedAppointment: DbRequestAppointmentSuccess? = null,
    val scheduledAppointment: DbScheduleAppointmentSuccess? = null,
)

data class Appointment(
    val count: Int,
    val details: List<Detail>
)

data class Detail(
    val uuid: String,
    val appointmentIdentifier: String?,
    val appointmentType: String?,
    val serviceUuid: String?,
    val serviceName: String?,
    val reasonNotes: String?,
    val status: String?,
    val requestedAppointmentDetails: RequestedAppointmentDetails? = null,
    val scheduledAppointmentDetails: ScheduledAppointmentDetails? = null
    
)

data class RequestedAppointmentDetails(
    val provider: String?,
    val requestedBy: String?,
    val requestedOn: String?
)

data class ScheduledAppointmentDetails(
    val location: DbLocation?,
    val provider: String?,
    val startDate: String?,
    val endDate: String?
)
data class DbTimeSlotData(
    val uuid: String,
    val startDate: String?,
    val endDate: String?,
    val appointmentBlock: DbAppointmentData?,
    val countOfAppointments: Int?,
    val unallocatedMinutes: Int?
)
data class DbAppointmentData(
    val uuid: String,
    val startDate: String?,
    val endDate: String?,
    val provider: DbPerson?,
    val location: DbLocation?,
    val appointmentTypes: List<DbTypesData>,
)
data class DbTypesData(
    val uuid: String,
    val display: String
)
data class DbReturnDetails(
    val count: Int,
    val results: Any
)
data class DbCancelAppointment(
    val uuid: String,
    val status: DbStatusData?
)
data class DbCancelRequestAppointmentResponse(
    @JsonProperty("uuid")
    val uuid: String?,
    @JsonProperty("status")
    val status: String?
)
data class DbCancelScheduledAppointmentResponse(
    @JsonProperty("uuid")
    val uuid: String?,
    @JsonProperty("status")
    val status: DbStatusData?
)
data class DbStatusData(
    @JsonProperty("code")
    val code: String?,
)
data class DbModifyAppointment(
    val timeSlot: String?,
    val appointmentType: String?,
    val reason: String?
)
data class DbModifyScheduledAppointmentResponse(
    @JsonProperty("uuid")
    val uuid: String?,
    @JsonProperty("display")
    val display: String?,
    @JsonProperty("appointmentType")
    val appointmentType: Dbtypes?,
    @JsonProperty("reason")
    val reason: String?,
    @JsonProperty("timeSlot")
    val timeSlot: DbTimeSlot?,
    @JsonProperty("status")
    val status: DbStatus?
)
data class DbModifyRequestAppointmentResponse(
    @JsonProperty("uuid")
    val uuid: String?,
    @JsonProperty("display")
    val display: String?,
    @JsonProperty("appointmentType")
    val appointmentType: Dbtypes?,
    @JsonProperty("notes")
    val notes: String?,
    @JsonProperty("status")
    val status: String?,
    @JsonProperty("requestedOn")
    val requestedOn: String?,
    @JsonProperty("provider")
    val provider: DbProviderResults?,
    @JsonProperty("location")
    val location: DbLocation?,
    @JsonProperty("requestedBy")
    val requestedBy: DbProviderResults?
)

// Visit History Data Classes
data class DbVisitResponse(
    @JsonProperty("results")
    val results: List<DbVisit>
)

data class DbVisit(
    @JsonProperty("uuid")
    val uuid: String?,
    @JsonProperty("display")
    val display: String?,
    @JsonProperty("startDatetime")
    val startDatetime: String?,
    @JsonProperty("stopDatetime")
    val stopDatetime: String?,
    @JsonProperty("visitType")
    val visitType: DbVisitType?,
    @JsonProperty("location")
    val location: DbVisitLocation?,
    @JsonProperty("patient")
    val patient: DbPatientData?,
    @JsonProperty("encounters")
    val encounters: List<DbEncounter>?,
    @JsonProperty("attributes")
    val attributes: List<DbVisitAttribute>?
)

data class DbVisitType(
    @JsonProperty("uuid")
    val uuid: String?,
    @JsonProperty("display")
    val display: String?
)

data class DbVisitLocation(
    @JsonProperty("uuid")
    val uuid: String?,
    @JsonProperty("display")
    val display: String?
)

data class DbEncounter(
    @JsonProperty("uuid")
    val uuid: String?,
    @JsonProperty("display")
    val display: String?,
    @JsonProperty("encounterDatetime")
    val encounterDatetime: String?,
    @JsonProperty("encounterType")
    val encounterType: DbEncounterType?,
    @JsonProperty("provider")
    val provider: DbProviderResults?
)

data class DbEncounterType(
    @JsonProperty("uuid")
    val uuid: String?,
    @JsonProperty("display")
    val display: String?
)

data class DbVisitAttribute(
    @JsonProperty("attributeType")
    val attributeType: DbAttributeType?,
    @JsonProperty("value")
    val value: String?
)

data class DbAttributeType(
    @JsonProperty("uuid")
    val uuid: String?,
    @JsonProperty("display")
    val display: String?
)

data class DbVisitHistoryDetail(
    val visitUuid: String?,
    val visitDisplay: String?,
    val visitStartDate: String?,
    val visitStopDate: String?,
    val visitType: String?,
    val location: DbLocation?,
    val provider: String?,
    val appointmentUuid: String?,
    val appointmentType: String?,
    val appointmentStatus: String?,
    val appointmentReason: String?,
    val encounters: List<DbEncounterInfo>?
)

data class DbEncounterInfo(
    val encounterUuid: String?,
    val encounterDisplay: String?,
    val encounterDate: String?,
    val encounterType: String?,
    val provider: String?
)

data class DbVisitHistoryResponse(
    val count: Int,
    val results: List<DbVisitHistoryDetail>
)
