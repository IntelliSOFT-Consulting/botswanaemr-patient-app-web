package com.intellisoft.botswanaemrappointments


import org.keycloak.KeycloakPrincipal
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern


class FormatterClass {

    fun getValue(): DbAppValues{
        val props = Properties()
        val inputStream = javaClass.classLoader
            .getResourceAsStream("application.properties")
        props.load(inputStream)
        val username = props.getProperty("openmrs.username")
        val password = props.getProperty("openmrs.password")
        val url = props.getProperty("openmrs.url")

        return DbAppValues( username, password, url)
    }

    //Convert date to string
    fun convertDateToString(date: Date?): String {

        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX")
        val outputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

        return if (date != null){
            val dateStr = inputFormat.parse(date.toString())
            outputFormat.format(dateStr)
        }else{
            ""
        }


    }

    fun getBaseUrl(
        params: MutableMap<String, String>,
        appointmentType: String,
        getAppsUrl: String,
        getAppsRequest: String,
        ): String {

        var baseUrl = ""
        var appRequest = getAppsRequest
        /**
         * Check the appointment type
         */
        if (appointmentType == AppointmentTypeData.SCHEDULE_APPOINTMENT.name) {
            baseUrl = getAppsUrl
        } else {
            appRequest += "?" + "v" + "=default"
            baseUrl = appRequest
        }

        params.forEach { (key, value) ->
            if (value != null) {
                baseUrl += "&$key=$value"
            }
        }
        return baseUrl

    }

    fun isEmailValid(emailAddress: String):Boolean{

        val emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\." +
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$"
        val pat = Pattern.compile(emailRegex)
        return pat.matcher(emailAddress).matches()
    }

    fun getOpenMrsDate(time: String): String? {

        return try {

            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            val output = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val d = sdf.parse(time)
            output.format(d)

        }catch (e: Exception){
            null
        }


    }
    fun getCurrentUserLogin(): Optional<String> {
        val securityContext = SecurityContextHolder.getContext()
        return Optional.ofNullable(securityContext.authentication)
            .map { authentication: Authentication ->
                if (authentication.principal is KeycloakPrincipal<*>) {
                    val keyCloakId = authentication.principal as KeycloakPrincipal<*>
                    return@map keyCloakId.toString()
                }
                null
            }
    }

    fun getResponse(results: Results): ResponseEntity<*>? {
        return when (results.code) {
            200, 201 -> {
                ResponseEntity.ok(results.details)
            }
            500 -> {
                ResponseEntity.internalServerError().body(results)
            }
            else -> {
                ResponseEntity.badRequest().body(DbResultsData(results.details.toString()))
            }
        }
    }






}