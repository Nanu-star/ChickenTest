package com.chickentest.auth;

import static org.junit.jupiter.api.Assertions.*;

import com.chickentest.config.AppSettings;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

class JwtServiceTest {

  private JwtService jwtService;

  @BeforeEach
  void setUp() {
    AppSettings appSettings = new AppSettings();
    // Test secret key (should be 256 bits for HS256)
    appSettings.setJwt(
        new AppSettings.Jwt(
            "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970", 86400000L));
    jwtService = new JwtService(appSettings);
  }

  @Test
  void testGenerateAndValidateToken() {
    UserDetails userDetails = new User("testuser", "password", Collections.emptyList());
    String token = jwtService.generateToken(userDetails);

    assertNotNull(token);
    assertEquals("testuser", jwtService.extractUsername(token));
    assertTrue(jwtService.isTokenValid(token, userDetails));
  }
}
