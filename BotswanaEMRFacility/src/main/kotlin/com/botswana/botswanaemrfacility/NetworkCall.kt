package com.botswana.botswanaemrfacility

import com.botswana.botswanaemrfacility.database.FacilityInfo
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.util.*

@Component
class NetworkCall() {

    var restTemplate = RestTemplate()

    fun getFacilityData(dbQueryParam: DbQueryParam?, type: String): Results{

        val details: Any?
        val code: Int

        val baseUrl = "https://mflld.gov.org.bw/api/v1/"

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers["Authorization"] =
            "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJrb2NoaWVuZ0BpbnRlbGxpc29mdGtlbnlhLmNvbSIsImNyZWF0ZWQiOjE2NDg3NDgwNDU2NzgsInJvbGVzIjpbeyJjcmVhdG9yIjpudWxsLCJjcmVhdGVkIjpudWxsLCJ1cGRhdGVyIjpudWxsLCJ1cGRhdGVkIjpudWxsLCJpZCI6ImRhMmZkOTE2LWQxMTMtNDYwMi05MWNhLTNlNWYzMzFmMWIwNiIsInJlZmVyZW5jZSI6bnVsbCwibmFtZSI6IkRldmVsb3BlciIsImRlc2NyaXB0aW9uIjoiRGV2ZWxvcGVyLCBhY2Nlc3MgTUZMIG9wZW4gYXBpIGZvciBpbnRlcm9wZXJhYmlsaXR5IiwiYXV0aG9yaXR5R3JvdXBzIjpbeyJjcmVhdG9yIjpudWxsLCJjcmVhdGVkIjpudWxsLCJ1cGRhdGVyIjpudWxsLCJ1cGRhdGVkIjpudWxsLCJpZCI6ImVmODgxNDdhLTg4NTUtNDg4ZC1hOGE1LWJlNGRlOTE3MTQyOCIsInJlZmVyZW5jZSI6bnVsbCwibmFtZSI6IkRFVkVMT1BFUl9BTEwiLCJkZXNjcmlwdGlvbiI6IkNhbiBhY2Nlc3MgYWxsIHRoZSBvcGVuIE1GTCBBcGlzIn1dfV0sImV4cCI6MzUzMTQ3NDgwNDUsImp0aSI6IjkxYTZkZTA5LWIwZjktMTFlYy1iZTU4LWNkMDY2NTJmNWY5MCJ9.Ofmxwd__T8OEvbdXsenlyeWxlPBkwBcaB8-qBhmF05Xe3XF7_zziZaafGMu9PumdWgnHLEW3Eh0MR0ulvG_zlA"

        val request: HttpEntity<*> = HttpEntity<Any?>(headers)

        println(type)

        val url = when (type) {
            "ALL" -> { "mfl/facility/all" }
            "CONSTITUENCY" -> { "mfl/facility/constituency" }
            "DISTRICT" -> { "mfl/facility/district/all" }
            "SERVICE" -> { "mfl/facility/service" }
            "TYPE" -> { "mfl/facility/type" }
            "DHMT" -> { "mfl/facility/type" }
            "DHMT-DISTRICT" -> { "mfl/facility/dhmt" }
            "INFRASTRUCTURE" -> { "mfl/facility/infrastructures" }
            "OWNERSHIP" -> { "mfl/facility/ownership" }
            "FACILITY_DETAILS_BY_CODE" -> {
                var facilityCode = ""
                var codeType = ""
                if (dbQueryParam != null){
                    facilityCode = dbQueryParam.param1
                    codeType = dbQueryParam.param2
                }
                "mfl/facility/$facilityCode/$codeType" }
            "FACILITY_PAGED" -> {
                var pageNo = ""
                var itemNo = ""
                if (dbQueryParam != null){
                    pageNo = dbQueryParam.param1
                    itemNo = dbQueryParam.param2
                }
                "mfl/facility/$pageNo/$itemNo/facility" }
            "FACILITY_DETAILS" -> {
                var facilityId = ""
                if (dbQueryParam != null){
                    facilityId = dbQueryParam.param1
                }
                "mfl/facility/$facilityId" }
            else -> { "mfl/facility/all" }
        }

        try {

            val responseRestTemplate = restTemplate.exchange(
                "$baseUrl$url", HttpMethod.GET, request,
                Any::class.java, 1)

            val statusCode = responseRestTemplate.statusCodeValue
            return if (statusCode == 302){
                val responseBody = responseRestTemplate.body

                if (responseBody != null){

                    val gson = Gson()
                    val newResponse = gson.toJson(responseBody)

                    return Results(200, newResponse)

                }else{

                    Results(400, "There was an issue")
                }

            }else{
                Results(400, "There was an issue")
            }

        }catch (e: Exception){

            val message = when (type) {
                "FACILITY_DETAILS_BY_CODE" -> {
                    "The facility details cannot be found from the provided code."
                }
                "FACILITY_PAGED" -> {
                    "We could not find the requested list of facilities"
                }
                "FACILITY_DETAILS" -> {
                    "The provided id does not have any facility associated to it."
                }else ->{
                    "There was an issue processing the request. Try again after sometime."
                }
            }

            return Results(400, message)
        }


    }



    private fun getResponse(type: String, newResponse: Array<Any>?):Results {

        var results : Any? = null
        var code = 200

        val gson = Gson()
        when (type) {
            "ALL" -> {

            }
            "CONSTITUENCY" -> { "mfl/facility/constituency" }
            "DISTRICT" -> {
                val districts = gson.toJson(newResponse)
                results = districts


            }
            "SERVICE" -> { "mfl/facility/service" }
            "TYPE" -> { "mfl/facility/type" }
            "DHMT" -> { "mfl/facility/type" }
            "DHMT-DISTRICT" -> { "mfl/facility/dhmt" }
            "INFRASTRUCTURE" -> { "mfl/facility/infrastructures" }
            "OWNERSHIP" -> { "mfl/facility/ownership" }
            else -> { "mfl/facility/all" }
        }

        return Results(code, results)
    }



}