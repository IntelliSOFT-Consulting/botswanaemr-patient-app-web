package com.intellisoft.botswanaemrauthentication;

import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import retrofit2.GsonConverterFactory;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

@SpringBootApplication
public class BotswanaEmrAuthenticationApplication {

    private FormatterClass formatterClass = new FormatterClass();

    OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();

    private String credentials = Credentials.basic(
            formatterClass.getValue().getUsername(),
            formatterClass.getValue().getPassword());


    public static void main(String[] args) {

        SpringApplication.run(BotswanaEmrAuthenticationApplication.class, args);
        System.setProperty("javax.net.ssl.trustStore", "classpath:server/certificate1.p12");
        System.setProperty("javax.net.ssl.trustStorePassword", "dYL5_Uuf");


    }

    //Creating bean keycloakConfigResolver
    @Bean
    public KeycloakSpringBootConfigResolver keycloakConfigResolver() {
        return new KeycloakSpringBootConfigResolver();
    }

    Interceptor interceptor = chain -> {
        Request request = chain.request().newBuilder()
                .header("Authorization", credentials).build();

        return chain.proceed(request);
    };



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
