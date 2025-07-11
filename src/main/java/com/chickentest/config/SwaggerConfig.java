package com.chickentest.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@Configuration
@OpenAPIDefinition(info = @Info(title="Mi API", version="1.0"),
                   security = @SecurityRequirement(name="bearerAuth"))
@SecurityScheme(name="bearerAuth", type=SecuritySchemeType.HTTP,
                scheme="bearer", bearerFormat="JWT", in=SecuritySchemeIn.HEADER)
public class SwaggerConfig {}