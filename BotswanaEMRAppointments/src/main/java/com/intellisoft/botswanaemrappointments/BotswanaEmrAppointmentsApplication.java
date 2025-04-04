package com.intellisoft.botswanaemrappointments;

import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import retrofit2.GsonConverterFactory;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;

//@EnableWebMvc
@SpringBootApplication
public class BotswanaEmrAppointmentsApplication {
    private FormatterClass formatterClass = new FormatterClass();

    OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();

    private String credentials = Credentials.basic(
            formatterClass.getValue().getUsername(),
            formatterClass.getValue().getPassword());


    public static void main(String[] args) {
        SpringApplication.run(BotswanaEmrAppointmentsApplication.class, args);
    }

    Interceptor interceptor = chain -> {
        Request request = chain.request().newBuilder().header("Authorization", credentials).build();

        return chain.proceed(request);
    };

    //Creating bean keycloakConfigResolver
    @Bean
    public KeycloakSpringBootConfigResolver keycloakConfigResolver() {
        return new KeycloakSpringBootConfigResolver();
    }


    @Bean
    public Retrofit retrofit() {
        clientBuilder.interceptors().add(interceptor);
        return new Retrofit.Builder()
                .baseUrl(formatterClass.getValue().getOpenMrsUrl())
                .client(clientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .addConverterFactory(JacksonConverterFactory.create())
                .build();
    }




}
