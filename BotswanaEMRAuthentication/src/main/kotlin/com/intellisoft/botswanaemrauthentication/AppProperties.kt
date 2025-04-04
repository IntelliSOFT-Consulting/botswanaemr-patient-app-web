package com.intellisoft.botswanaemrauthentication

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class AppProperties {

    @Value("\${notification}")
    lateinit var notificationUrl: String
}