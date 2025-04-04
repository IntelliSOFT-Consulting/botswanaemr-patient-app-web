package com.botswana.botswanaemrfacility

import com.botswana.botswanaemrfacility.database.FacilityInfo
import com.botswana.botswanaemrfacility.database.FacilityInfoRepository
import com.botswana.botswanaemrfacility.service.FacilityServiceImpl
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.function.Function

class FormatterClass {

    fun addFacility(facilityService: FacilityServiceImpl, facilityInfoRepository: FacilityInfoRepository){

        CoroutineScope(Dispatchers.IO).launch {
            addFacilityBac(facilityService, facilityInfoRepository)
        }
    }
    private suspend fun addFacilityBac(facilityService: FacilityServiceImpl, facilityInfoRepository: FacilityInfoRepository){
        coroutineScope {
            launch(Dispatchers.IO){
                try {
                    fetchFromMfl(facilityService, facilityInfoRepository)
                }catch (e: Exception){
                    println("*-*- $e")
                }

            }
        }
    }

    private fun fetchFromMfl(facilityService: FacilityServiceImpl, facilityInfoRepository: FacilityInfoRepository) {

        val facilities= getValueData(facilityService.allFacilities, FacilityField.FACILITY_DETAILS.name)
        val facilityConstituency= getValueData(facilityService.allConstituencies, FacilityField.FACILITY_CONSTITUENCY.name)
        val facilityDistricts= getValueData(facilityService.allDistricts, FacilityField.FACILITY_DISTRICTS.name)
        val facilityServices= getValueData(facilityService.services, FacilityField.FACILITY_SERVICES.name)
        val facilityTypes= getValueData(facilityService.facilityTypes, FacilityField.FACILITY_TYPES.name)
        val facilityDhmt= getValueData(facilityService.allDhmt, FacilityField.FACILITY_DHMT.name)
        val facilityDistrictDhmt= getValueData(facilityService.districtDhmt, FacilityField.FACILITY_DHMT_DISTRICT.name)
        val facilityInfrastructure= getValueData(facilityService.allInfrastructure, FacilityField.FACILITY_INFRASTRUCTURES.name)

        val facilityInfoList = listOf(
            facilities, facilityConstituency, facilityDistricts, facilityServices,
            facilityTypes, facilityDhmt, facilityDistrictDhmt, facilityInfrastructure
        )
        for (facilityInfo in facilityInfoList) {
            saveFacilityInfo(facilityInfo, facilityInfoRepository)
        }
    }

    private fun saveFacilityInfo(facilityInfo: FacilityInfo?, facilityInfoRepository: FacilityInfoRepository): FacilityInfo? {

        println("------------- $facilityInfo")

        return if (facilityInfo != null){

            val gson = Gson()

            val fieldName = facilityInfo.fieldName
            val jsonField = facilityInfo.dbJson
            val facilityJson = gson.toJson(jsonField)

            val facilityInfoNew = FacilityInfo(fieldName, DbJson(facilityJson))

            val isFacilityDetails = facilityInfoRepository.existsByFieldName(fieldName)
            if (isFacilityDetails) {
                println("****** ${facilityInfo.dbJson}")
                updateFacilityInfo(facilityInfoNew, facilityInfoRepository)
            } else {
                println("====== ${facilityInfoNew.dbJson}")
                facilityInfoRepository.save(facilityInfoNew)
            }
        }else{
            null
        }

    }

    private fun updateFacilityInfo(facilityInfo: FacilityInfo, facilityInfoRepository: FacilityInfoRepository): FacilityInfo? {
        val id = facilityInfo.id
        return facilityInfoRepository.findById(id)
            .map { facilityInfoOld: FacilityInfo ->
                facilityInfoOld.fieldName = facilityInfo.fieldName
                facilityInfoOld.dbJson = facilityInfo.dbJson
                facilityInfoRepository.save(facilityInfoOld)
            }.orElse(null)
    }

    private fun getValueData(results: Results, facilityDetails: String): FacilityInfo? {
        val facilityInfo = FacilityInfo()
        val code = results.code
        return if (code == 200) {
            facilityInfo.fieldName = facilityDetails
            facilityInfo.dbJson = DbJson(results.details)
            facilityInfo
        } else {
            null
        }
    }

}