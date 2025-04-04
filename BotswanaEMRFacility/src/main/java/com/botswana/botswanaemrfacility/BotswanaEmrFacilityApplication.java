package com.botswana.botswanaemrfacility;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import retrofit2.GsonConverterFactory;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;

@SpringBootApplication
public class BotswanaEmrFacilityApplication {

    OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();


    public static void main(String[] args) {
        SpringApplication.run(BotswanaEmrFacilityApplication.class, args);
    }

    Interceptor interceptor = chain -> {
        Request request = chain.request().newBuilder().header("Authorization",
                "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJrb2NoaWVuZ0BpbnRlbGxpc29mdGtlbnlhLmNvbSIsImNyZWF0ZWQiOjE2NDg3NDgwNDU2NzgsInJvbGVzIjpbeyJjcmVhdG9yIjpudWxsLCJjcmVhdGVkIjpudWxsLCJ1cGRhdGVyIjpudWxsLCJ1cGRhdGVkIjpudWxsLCJpZCI6ImRhMmZkOTE2LWQxMTMtNDYwMi05MWNhLTNlNWYzMzFmMWIwNiIsInJlZmVyZW5jZSI6bnVsbCwibmFtZSI6IkRldmVsb3BlciIsImRlc2NyaXB0aW9uIjoiRGV2ZWxvcGVyLCBhY2Nlc3MgTUZMIG9wZW4gYXBpIGZvciBpbnRlcm9wZXJhYmlsaXR5IiwiYXV0aG9yaXR5R3JvdXBzIjpbeyJjcmVhdG9yIjpudWxsLCJjcmVhdGVkIjpudWxsLCJ1cGRhdGVyIjpudWxsLCJ1cGRhdGVkIjpudWxsLCJpZCI6ImVmODgxNDdhLTg4NTUtNDg4ZC1hOGE1LWJlNGRlOTE3MTQyOCIsInJlZmVyZW5jZSI6bnVsbCwibmFtZSI6IkRFVkVMT1BFUl9BTEwiLCJkZXNjcmlwdGlvbiI6IkNhbiBhY2Nlc3MgYWxsIHRoZSBvcGVuIE1GTCBBcGlzIn1dfV0sImV4cCI6MzUzMTQ3NDgwNDUsImp0aSI6IjkxYTZkZTA5LWIwZjktMTFlYy1iZTU4LWNkMDY2NTJmNWY5MCJ9.Ofmxwd__T8OEvbdXsenlyeWxlPBkwBcaB8-qBhmF05Xe3XF7_zziZaafGMu9PumdWgnHLEW3Eh0MR0ulvG_zlA").build();

        return chain.proceed(request);
    };

    @Bean
    public Retrofit retrofit() {
        clientBuilder
                .followRedirects(true)
                .followSslRedirects(false)
                .interceptors()
                .add(interceptor);

        return new Retrofit.Builder()
                .baseUrl("https://mflld.gov.org.bw/api/v1/")
                .client(clientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .addConverterFactory(JacksonConverterFactory.create())
                .build();
    }
}
