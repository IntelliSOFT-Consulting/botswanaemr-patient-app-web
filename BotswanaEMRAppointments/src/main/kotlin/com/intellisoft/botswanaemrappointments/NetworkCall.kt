package com.intellisoft.botswanaemrappointments

import kotlinx.coroutines.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import retrofit2.Retrofit
import javax.annotation.PostConstruct


@Component
class NetworkCall() {


    var restTemplate = RestTemplateConfig()

    @Autowired
    private val retrofit: Retrofit? = null

    private var networkRequestInterface: NetworkRequestInterface? = null

    @PostConstruct
    fun setup() {
        networkRequestInterface = retrofit?.create(NetworkRequestInterface::class.java)
    }

    fun getUserDetails(keycloakId: String, url: String) = runBlocking { getBacUserDetails(keycloakId, url) }
    private suspend fun getBacUserDetails(keycloakId: String, url: String) : UserDetails?{



        val authenticationUrl = url
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val url = "/service/authentication/user/$keycloakId"

        return try {

            val results = withContext(Dispatchers.IO) {
                restTemplate.restTemplate().getForEntity("$authenticationUrl$url", UserDetails::class.java, 1)
            }
            if (results.statusCode == HttpStatus.OK){
                println("----- ${results.body}")
                results.body
            }else{
                null
            }

        }catch (e: Exception){
            e.printStackTrace()
            null
        }

    }

    fun createNotification(type: String, dbNotification: DbNotification, url: String) {

        CoroutineScope(Dispatchers.IO).launch { sendNotification(type, dbNotification, url) }

    }
    private suspend fun sendNotification(type: String, dbNotification: DbNotification, url: String){

        coroutineScope {
            launch(Dispatchers.IO){
                val notificationUrl = url
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


                val results = restTemplate.restTemplate().postForObject(
                    "$notificationUrl$url", dbNotification, Any::class.java
                )
                println("------notification $notificationUrl")
            }
        }

    }

}