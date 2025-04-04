package com.intellisoft.botswanaemrambulance

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class AppProperties {

    @Value("\${authentication}")
    lateinit var authenticationUrl: String

    @Value("\${notification}")
    lateinit var notificationUrl: String
}