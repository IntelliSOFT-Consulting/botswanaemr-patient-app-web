package com.intellisoft.botswanaemrnotifications

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import javax.mail.internet.MimeMessage

@Component
class NetworkCall() {


    var restTemplate = RestTemplate()


    fun getUserDetails(keycloakId: String) : PatientDetails?{

        val notificationUrl = "http://172.105.157.130:8081/auths"
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val url = "/service/authentication/user/$keycloakId"

        return try {

            val results = restTemplate.getForEntity("$notificationUrl$url", PatientDetails::class.java, 1)
            if (results.statusCode == HttpStatus.OK){
                println("----- ${results.body}")
                results.body
            }else{
                null
            }

        }catch (e: Exception){
            null
        }



    }

    fun sendEmail(emailSender: JavaMailSender, patientDetails: DbNotification, templateEngine: TemplateEngine){

        CoroutineScope(Dispatchers.IO).launch {
            sendEmailBackground(emailSender,patientDetails, templateEngine)
        }

    }

    private suspend fun sendEmailBackground(
        emailSender: JavaMailSender,
        dbNotification: DbNotification,
        templateEngine: TemplateEngine
    ) {

        coroutineScope {
            launch(Dispatchers.IO){

                //Get User Details
                val userId = dbNotification.userId
                val patientDetails = getUserDetails(userId)
                if (patientDetails != null){

                    val emailAddress = patientDetails.emailAddress
                    val username = patientDetails.username

                    val greeting = "Hi, $username, \n\n"

                    val title = dbNotification.title
                    val message = dbNotification.message
                    val notificationType = dbNotification.notificationType
                    val processType = dbNotification.process

                    val context = Context()
                    context.setVariable("title", title)
                    context.setVariable("subject", processType)
                    context.setVariable("greeting", greeting)
                    context.setVariable("message", message)

                    val process: String = templateEngine.process("notifications", context)
                    val mimeMessage: MimeMessage = emailSender.createMimeMessage()
                    val helper = MimeMessageHelper(mimeMessage)
                    helper.setSubject(processType)
                    helper.setText(process, true)
                    helper.setTo(emailAddress)
                    emailSender.send(mimeMessage)

                    println("********")

                }




            }

        }

    }


}