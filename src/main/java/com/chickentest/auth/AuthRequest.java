package com.chickentest.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthRequest {
  @NotBlank
  @Schema(defaultValue = "system")
  private String username;

  @NotBlank
  @Schema(defaultValue = "admin")
  private String password;
}
