package com.itop.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("iTop Java CMDB API")
                        .version("1.0.0")
                        .description("Modern CMDB Platform built with Spring Boot and PostgreSQL")
                        .contact(new Contact()
                                .name("iTop Java")
                                .email("support@itop.local"))
                        .license(new License()
                                .name("AGPL-3.0")
                                .url("https://opensource.org/licenses/AGPL-3.0")));
    }
}