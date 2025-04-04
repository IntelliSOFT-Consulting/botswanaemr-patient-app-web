package com.intellisoft.botswanaemrambulance

import org.springframework.http.ResponseEntity

class FormatterClass {

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