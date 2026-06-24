package com.restaurant.billservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI billServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Bill Service API")
                        .description("Restaurant Management System — Bill & Payment Microservice. " +
                                "Handles automated bill generation from kitchen events, " +
                                "GST/tax calculation, and payment gateway integration.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Restaurant Dev Team")
                                .email("dev@restaurant.com"))
                        .license(new License()
                                .name("Private")))
                .servers(List.of(
                        new Server().url("http://localhost:8084").description("Local Dev")
                ));
    }
}
