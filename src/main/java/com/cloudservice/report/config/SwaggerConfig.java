package com.cloudservice.report.config;

import org.springdoc.core.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.In;

@Configuration
public class SwaggerConfig {
	
	public static final String AUTHORIZATION = "Authorization";
	
	public static final String GLOBAL = "global";
	public static final String HEADER = "header";
	public static final String JWT = "JWT";

    static {
        SpringDocUtils.getConfig().addAnnotationsToIgnore(Components.class);
    }

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(new Info().title("Validation Service").version("v1"))
                .addSecurityItem(new SecurityRequirement().addList("JWT"))
                .components(new Components().addSecuritySchemes("JWT",
                        new SecurityScheme().type(SecurityScheme.Type.APIKEY).in(In.HEADER).name(AUTHORIZATION)));
    }
}
