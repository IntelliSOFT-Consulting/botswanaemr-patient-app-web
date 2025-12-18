package com.intellisoft.botswanaemrauthentication

import org.springframework.data.repository.query.Param
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface NetworkRequestInterface {

    @POST("openmrs/ws/rest/v1/person")
    fun createPerson(@Body dbPerson: DbPerson): Call<DbPersonSuccess>

    @POST("openmrs/ws/rest/v1/patient")
    fun createPatientFromPerson(@Body dbPatient: DbPatient): Call<DbPersonSuccess>

    @POST("api/v1/create")
    fun createNotification(@Body dbNotification:DbNotification):Call<Results>

    @GET("v1/patient")
    suspend fun getUserDetails(@Query("q") q: String, @Query("v") v: String):Response<DbOpenMrsResult>
    
    @GET("v1/patient")
    suspend fun getPatientDetailsFull(@Query("q") q: String, @Query("v") v: String):Response<DbOpenMrsPatientResult>

    @GET("emrapi/conditionhistory")
    suspend fun getConditions(@Query("patientUuid") patientUuid: String):Response<List<DbConditionsData>>

    @GET("v1/patient/{patient}/allergy")
    suspend fun getAllergy(@Path("patient") patient: String):Response<DbAllergyResults>

    @GET("v1/order")
    suspend fun getDrugs(@Query("patient") patient: String):Response<DbDrugs>

    @GET("v1/order/{order}")
    suspend fun getDrugsDetails(@Path("order") order: String):Response<DbDrugsDetails>

    @GET("v1/obs")
    suspend fun getVitals(@Query("patient") patient: String, @Query("v") v: String):Response<DbVitalsResults>

    @GET("v1/visit")
    suspend fun getVisits(@Query("patient") patient: String, @Query("v") v: String):Response<DbVisitResponse>

    @GET("v1/visit/{visit}")
    suspend fun getVisitById(@Path("visit") visit: String, @Query("v") v: String):Response<DbVisitRaw>

}