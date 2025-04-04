package com.intellisoft.botswanaemrambulance

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate


@Component
class NetworkCall() {

    @Autowired
    lateinit var appProperties: AppProperties

    var restTemplate = RestTemplateConfig().restTemplate()


    fun getUserDetailsEmailAddress(emailAddress: String): PatientDetails? {

        val authenticationUrl = appProperties.authenticationUrl
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val url = "/service/authentication/user/search/$emailAddress"
        val baseUrl = authenticationUrl + url

        return try {

            println("----->baseUrl ${baseUrl}")

            val results = restTemplate.getForEntity(baseUrl, PatientDetails::class.java, 1)
            if (results.statusCode == HttpStatus.OK) {
                println("-----> ${results.body}")
                println("-----> ${results.statusCode}")
                results.body
            } else {
                null
            }

        } catch (e: Exception) {
            println("----->> ${e.printStackTrace()}")

            null
        }


    }

    fun getUserDetailsKeyCloak(keycloakId: String): PatientDetails? {

        val notificationUrl = appProperties.authenticationUrl
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val url = "/service/authentication/user/$keycloakId"

        return try {

            val results = restTemplate.getForEntity("$notificationUrl$url", PatientDetails::class.java, 1)
            if (results.statusCode == HttpStatus.OK) {
                println("----- ${results.body}")
                results.body
            } else {
                null
            }

        } catch (e: Exception) {
            null
        }


    }

    fun updateRole(dbUserRole: DbUserRole): Any? {

        val authenticationUrl = appProperties.authenticationUrl
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val url = "/service/authentication/user/update-role"

        val baseUrl = authenticationUrl + url

        return try {

            val results = restTemplate.postForEntity(baseUrl, dbUserRole, Any::class.java)
            if (results.statusCode == HttpStatus.OK) {
                println("----- ${results.body}")
                results.body
            } else {
                println("----- ${results.statusCode}")
                null
            }

        } catch (e: Exception) {
            println("----- $e")
            null
        }

    }

    //Create Notification
    fun createNotification(type: String, dbNotification: DbNotification) {

        CoroutineScope(Dispatchers.IO).launch { sendNotification(type, dbNotification) }

    }
    private suspend fun sendNotification(type: String, dbNotification: DbNotification){

        coroutineScope {
            launch(Dispatchers.IO){
                val notificationUrl = appProperties.notificationUrl
                val headers = HttpHeaders()
                headers.contentType = MediaType.APPLICATION_JSON
                val url = when (type) {
                    "CREATE_NOTIFICATION" -> {
                        "/service/notification/create"
                    }
                    else -> {
                        ""
                    }
                }


                val results = restTemplate.postForObject(
                    "$notificationUrl$url", dbNotification, Any::class.java
                )
                println("------ $results")
            }
        }

    }


}