package com.intellisoft.botswanaemrnotifications

import com.fasterxml.jackson.annotation.JsonProperty

data class Results(
    val code: Int,
    val message: Any)

data class DbNotification(
    val title: String,
    val message: String,
    val userId: String,
    val notificationType: String,
    val process: String
)

data class DbResults(
    val message: String
)
data class PatientDetails(

    @JsonProperty("id")
    val id: String,
    @JsonProperty("keycloakId")
    val keycloakId: String?,
    @JsonProperty("phoneNumber")
    val phoneNumber: String,
    @JsonProperty("gender")
    val gender: String,
    @JsonProperty("emailAddress")
    val emailAddress: String,
    @JsonProperty("username")
    val username: String,
    @JsonProperty("openMrsId")
    val openMrsId: String?
)

data class DbDynamicResponse(
    val count: Int,
    val next: String? = null,
    val previous: String? = null,
    val details: Any
)