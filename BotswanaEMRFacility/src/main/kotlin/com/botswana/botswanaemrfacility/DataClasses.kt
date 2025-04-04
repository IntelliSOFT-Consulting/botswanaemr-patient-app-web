package com.botswana.botswanaemrfacility

import lombok.AllArgsConstructor
import lombok.NoArgsConstructor

data class Error(var timestamp: String, var status: String, var error: String)
data class Results(val code: Int, val details: Any?)

@NoArgsConstructor
@AllArgsConstructor
data class DbJson(
    val jsonb:Any?
)

data class DbFacility(
    val facilityName: String,
    val newFacilityCode: String,
    val oldFacilityCode: String,
    val isUrbanOrRural: String,
    val status: String,
    val facilityStatus: String,
    val telephone: String,
    val lat: String,
    val lng: String,
    val facilityOwner: String,

    val facilityType: FacilityType,
    val dhmt: DbDhmt,
    val constituency: DbConstituency,
    val facilityServices: FacilityServices,
    val physicalAddress: PhysicalAddress

)

data class FacilityType(val name:String, val score: String)
data class FacilityServices(val id: String, val name: String)
data class PhysicalAddress(val cityTown: String, val ward: String)

data class DbConstituency(
    val id:String,
    val name: String)

data class DbDistricts(
    val id: String,
    val name: String,
    val code: String
)
data class DbService(
    val name: String
)
data class DbType(
    val name: String,
    val score: String
)
data class DbDhmt(
    val id: String,
    val name: String)
data class DbDhmtDistrict(
    val dhmtDto: DhmtDto,
    val districtDto: DistrictDto,
)
data class DhmtDto(val id: String, val name: String)
data class DistrictDto(val id: String, val name: String)
data class DbQueryParam(
    val param1: String,
    val param2: String
)
enum class FacilityField(){
    FACILITY_DETAILS,
    FACILITY_DISTRICTS,
    FACILITY_SERVICES,
    FACILITY_CONSTITUENCY,
    FACILITY_TYPES,
    FACILITY_DHMT,
    FACILITY_DHMT_DISTRICT,
    FACILITY_INFRASTRUCTURES,
}