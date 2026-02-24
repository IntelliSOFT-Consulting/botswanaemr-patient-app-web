package com.intellisoft.botswanaemrappointments

import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import retrofit2.Response

import retrofit2.http.*

interface NetworkRequestInterface {

    //Create Patient
    @POST("botswanaemr/registration")
    suspend fun createPatient(@Body dbPatientDetails: DbPatientDetails): Response<DbCreatePatientSuccess>

    //Patient Details
    @GET("person/{patientId}")
    suspend fun getPatientDetails(@PathVariable("patientId") patientId: String): Response<DbPersonDetails>

    //Providers List
    @GET("provider")
    suspend fun getProviders(@Query("q") q: String, @Query("v") v: String): Response<DbProvider>

    //Hospital Locations
    @GET("location")
    suspend fun getFacilityLocations(): Response<DbLocations>

    //Get Facility
    @GET("openmrs/q/locationHierarchy.json?selectLeafOnly=false&id=0")
    suspend fun getFacilities(): Response<List<DbFacilities>>

    //Hospital Services
    @GET("appointmentscheduling/appointmenttype?")
    suspend fun getFacilityServices(@Query("v") v: String): Response<DbServiceTypes>

    //Timeslots
    @GET("appointmentscheduling/timeslot")
    suspend fun getTimeSlots(@Query("limit") limit: Int, @Query("v") v: String, ): Response<DbTimeSlotResult>

    //Timeslot Details
    @GET("appointmentscheduling/timeslot/{timeslotId}")
    suspend fun getTimeSlotDetails(@Path("timeslotId") timeslotId: String): Response<DbTimeSlot>

    //Appointment Types
    @GET("appointmentscheduling/appointmenttype?")
    suspend fun getAppointmentTypes(@Query("limit") limit: Int, @Query("v") v: String, ): Response<DbServiceTypes>

    //Patient Appointments
    @GET("appointmentscheduling/appointment")
    suspend fun getPatientAppointments(
        @Query("fromDate") fromDate: String,
        @Query("patient") patient: String,
        @Query("toDate") toDate: String,
        @Query("v") v: String): Response<DbPatientAppointmentData>

    //Schedule New Appointment
    @POST("appointmentscheduling/appointmentallowingoverbook")
    suspend fun scheduleNewAppointment(@Body dbScheduleNewAppointment: DbScheduleNewAppointment):
            Response<DbSuccessAppointment>

    //Patient Visits
    @GET("visit")
    suspend fun getPatientVisits(
        @Query("patient") patient: String,
        @Query("v") v: String,
        @Query("includeInactive") includeInactive: Boolean = false
    ): Response<DbVisitResponse>

}
