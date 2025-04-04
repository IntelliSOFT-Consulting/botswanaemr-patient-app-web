package com.intellisoft.botswanaemrnotifications

import org.keycloak.KeycloakPrincipal
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import java.util.*


class FormatterClass {

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
                ResponseEntity.ok(results.message)
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