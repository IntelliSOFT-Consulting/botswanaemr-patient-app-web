package com.intellisoft.botswanaemrauthentication

import com.intellisoft.botswanaemrauthentication.authentication.entity.PatientDetails
import com.intellisoft.botswanaemrauthentication.authentication.service_class.PatientDetailsServiceImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.keycloak.KeycloakPrincipal
import org.springframework.http.ResponseEntity
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import javax.mail.internet.MimeMessage
import kotlin.collections.ArrayList


@Component
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

    //Convert Date
    fun convertDate(dateString: String):String{

        val formats = arrayOf("dd-MM-yyyy", "dd-MMM-yyyy", "yyyy-MM-dd", "dd-MMMM-yyyy")
        var dateFormat: SimpleDateFormat? = null
        for (format in formats) {
            dateFormat = SimpleDateFormat(format)
            dateFormat.isLenient = false
            try {
                dateFormat.parse(dateString)
                break
            } catch (e: ParseException) {
                dateFormat = null
            }
        }
        val date = dateFormat?.parse(dateString)
        return if (date != null) {
            val newDateFormat = SimpleDateFormat("dd-MMM-yyyy")
            newDateFormat.format(date)
        } else {
            dateString
        }

    }

    fun isEmailValid(emailAddress: String):Boolean{

        val emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\." +
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$"
        val pat = Pattern.compile(emailRegex)
        return pat.matcher(emailAddress).matches()
    }

    fun isPasswordMatch(password:String, confirmPass: String): Boolean{
        return password == confirmPass
    }

    fun getResetPasswordOtp(n: Int):String{

        // Using numeric values
        val rnd = Random()
        val number = rnd.nextInt(999999)

        return String.format("%06d", number);

    }

    fun getVerificationLink(n: Int):String{

        val alphaNumericString : String = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "0123456789" + "abcdefghijklmnopqrstuvxyz"
        val sb = StringBuilder(n)
        for (i in 0 until n) {

            sb.append(alphaNumericString[ (((alphaNumericString.length * Math.random()).toInt())) ])
        }
        return sb.toString()
    }

    fun sendResetPasswordMail(emailSender: JavaMailSender, patientDetails: PatientDetails, otp: String, templateEngine: TemplateEngine,){
        CoroutineScope(Dispatchers.IO).launch {
            sendResetOtp(emailSender,patientDetails, otp, templateEngine)
        }
    }
    private suspend fun sendResetOtp(
        emailSender: JavaMailSender,
        patientDetails: PatientDetails,
        otp: String,
        templateEngine: TemplateEngine,
        ){
        coroutineScope {
            launch(Dispatchers.IO){

                val firstName = patientDetails.firstName
//                val messageDetails = "Hi $firstName, someone has requested to change your password. " +
//                        "Use this OTP code $otp to reset it. The OTP code will expire in 5 minutes. " +
//                        "If you did not make the request, Ignore this mail."

                val emailAddress = patientDetails.emailAddress
//                val msg = SimpleMailMessage()
//                msg.setTo(emailAddress);
//                msg.subject = "Use this OTP code to reset your password."
//                msg.text = messageDetails
//                emailSender.send(msg);


                val subject = "Password Reset OTP"
                val greeting = "Hi $firstName, "
                val action = otp
                val message = "Use this OTP code to reset your password. The OTP code will expire in 5 minutes. " +
                        "If you did not make the request, Ignore this mail."

                val context = Context()
                context.setVariable("subject", subject)
                context.setVariable("greeting", greeting)
                context.setVariable("message", message)
                context.setVariable("action", action)

                val process: String = templateEngine.process("notifications", context)
                val mimeMessage: MimeMessage = emailSender.createMimeMessage()
                val helper = MimeMessageHelper(mimeMessage)
                helper.setSubject(subject)
                helper.setText(process, true)
                helper.setTo(emailAddress)
                emailSender.send(mimeMessage)

            }
        }
    }



    fun sendMail(emailSender: JavaMailSender, templateEngine: TemplateEngine, patientDetails: PatientDetails, baseUrl: String, link: String){

        CoroutineScope(Dispatchers.IO).launch {
            sendRegLink(emailSender,patientDetails, baseUrl,templateEngine, link)
        }
    }
    private suspend fun sendRegLink(
        emailSender: JavaMailSender,
        patientDetails: PatientDetails,
        baseUrl: String,
        templateEngine: TemplateEngine,
        link: String){
        coroutineScope {
            launch(Dispatchers.IO){

                val id = patientDetails.id

                val verificationLink = baseUrl+"auths/api/v1/verify-link/?link=$link&req_id=$id"

                val emailAddress = patientDetails.emailAddress
                val name = patientDetails.firstName
//                val msg = SimpleMailMessage()
//                msg.setTo(emailAddress);
//                msg.subject = "Use this to verify your email address in 5 minutes."
//                msg.text = verificationLink
//                emailSender.send(msg);

                val subject = "Email Verification"
                val greeting = "Hi $name, "
                val action = verificationLink
                val message = "Use this to verify your email address in 5 minutes.\n\n$action \n\n " +
                        "If you did not initiate this process please ignore the message"

                val context = Context()
                context.setVariable("subject", subject)
                context.setVariable("greeting", greeting)
                context.setVariable("message", message)
                context.setVariable("action", "")

                val process: String = templateEngine.process("notifications", context)
                val mimeMessage: MimeMessage = emailSender.createMimeMessage()
                val helper = MimeMessageHelper(mimeMessage)
                helper.setSubject(subject)
                helper.setText(process, true)
                helper.setTo(emailAddress)
                emailSender.send(mimeMessage)

            }
        }
    }

    fun  createPerson(patientDetailsServiceImpl: PatientDetailsServiceImpl, dbPerson: DbPerson, systemUUID: String){

        CoroutineScope(Dispatchers.IO).launch {
            createPersonBackground(patientDetailsServiceImpl,dbPerson, systemUUID)
        }
    }
    private suspend fun createPersonBackground(patientDetailsServiceImpl: PatientDetailsServiceImpl,
                                               dbPerson: DbPerson, systemUUID: String){
        coroutineScope {
            launch(Dispatchers.IO){

//                val results = patientDetailsServiceImpl.createPerson(dbPerson)
//
//                val code = results.code
//                if (code == 200){
//
//                    val uuid = results.message.toString()
//
//                    val dbIdentifiers = DbIdentifiers(systemUUID,
//                        "UUID",
//                        "8d6c993e-c2cc-11de-8d13-0010c6dffd0f",false)
//                    val dbIdentifiersList = ArrayList<DbIdentifiers>()
//                    dbIdentifiersList.add(dbIdentifiers)
//
//                    val dbPatient = DbPatient(uuid, dbIdentifiersList)
//
//                    createPatient(patientDetailsServiceImpl, dbPatient)
//
//                }

            }
        }
    }

    fun  createPatient(patientDetailsServiceImpl: PatientDetailsServiceImpl, dbPatient: DbPatient){

        CoroutineScope(Dispatchers.IO).launch {
            createPatientBackground(patientDetailsServiceImpl,dbPatient)
        }
    }
    private suspend fun createPatientBackground(patientDetailsServiceImpl: PatientDetailsServiceImpl,
                                               dbPatient: DbPatient){
        coroutineScope {
            launch(Dispatchers.IO){

//                patientDetailsServiceImpl.createPatientFromPerson(dbPatient)

            }
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
            200 -> ResponseEntity.ok(results.message)
            201 -> {
                val message = results.message
                val dbResults = DbResults(message.toString())
                ResponseEntity.ok(dbResults)
            }
            500 -> {
                ResponseEntity.internalServerError().body(results)
            }
            else -> {
                ResponseEntity.badRequest().body(DbResults(results.message.toString()))
            }
        }
    }


}