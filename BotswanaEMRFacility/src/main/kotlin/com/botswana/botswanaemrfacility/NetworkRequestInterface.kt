package com.botswana.botswanaemrfacility

import retrofit2.Call
import retrofit2.http.GET

interface NetworkRequestInterface {

    @GET("mfl/facility/all")
    fun getAllFacility(): Call<List<DbFacility>>

    @GET("mfl/facility/constituency")
    fun getConstituency(): Call<List<DbConstituency>>

    @GET("mfl/facility/district/all")
    fun getDistrict(): Call<List<DbDistricts>>

    @GET("mfl/facility/service")
    fun getService(): Call<List<DbService>>

    @GET("mfl/facility/type")
    fun getTypes(): Call<List<DbType>>

    @GET("mfl/facility/dhmt")
    fun getDhmt(): Call<List<DbDhmt>>

    @GET("mfl/facility/dhmt-district")
    fun getDhmtDistrict(): Call<List<DbDhmtDistrict>>

    //Empty List
    @GET("mfl/facility/infrastructures")
    fun getInfrastructure(): Call<List<DbFacility>>

    //Throws server error
    @GET("mfl/facility/ownership")
    fun getOwnership(): Call<List<DbFacility>>


}