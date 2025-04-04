package com.intellisoft.botswanaemrnotifications

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class AppProperties {

    @Value("\${authentication}")
    lateinit var authenticationUrl: String
}