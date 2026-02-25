package com.intellisoft.botswanaemrauthentication

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "openmrs")
open class OpenMrsProperties {
    lateinit var username: String
    lateinit var password: String
    lateinit var url: String
}