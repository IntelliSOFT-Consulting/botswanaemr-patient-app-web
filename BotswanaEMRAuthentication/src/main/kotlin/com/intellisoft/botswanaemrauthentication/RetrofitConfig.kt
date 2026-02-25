package com.intellisoft.botswanaemrauthentication

import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory

@Configuration
open class RetrofitConfig(
    private val openMrsProperties: OpenMrsProperties
) {

    @Bean
    open fun retrofit(): Retrofit {

        val basicAuth = Credentials.basic(
            openMrsProperties.username,
            openMrsProperties.password)

        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request()
                    .newBuilder()
                    .header("Authorization", basicAuth)
                    .header("Accept", "application/json")
                    .build()

                chain.proceed(request)
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(openMrsProperties.url)
            .client(client)
            .addConverterFactory(JacksonConverterFactory.create())
            .build()
    }
}