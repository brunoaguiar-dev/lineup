package com.lineup.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class OpenApiConfig {

    @Bean
    OpenAPI lineupOpenApi() {
        return new OpenAPI().info(new Info()
                .title("LineUp")
                .version("v1")
                .description("Gestão e agendamento de aulas para escola de surf."));
    }
}
