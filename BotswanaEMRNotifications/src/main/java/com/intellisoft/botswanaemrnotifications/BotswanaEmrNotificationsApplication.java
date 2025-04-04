package com.intellisoft.botswanaemrnotifications;

import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BotswanaEmrNotificationsApplication {

    public static void main(String[] args) {
        SpringApplication.run(BotswanaEmrNotificationsApplication.class, args);
    }

    //Creating bean keycloakConfigResolver
    @Bean
    public KeycloakSpringBootConfigResolver keycloakConfigResolver() {
        return new KeycloakSpringBootConfigResolver();
    }

}
