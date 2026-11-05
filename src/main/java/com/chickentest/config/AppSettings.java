package com.chickentest.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
@Data
public class AppSettings {

  private Jwt jwt;
  private Security security;

  public record Jwt(@NotBlank String secret, @DefaultValue("86400000") long expiration) {}

  public record Security(@NotNull Admin admin) {}

  public record Admin(@NotBlank String username, @NotBlank String password) {}
}
