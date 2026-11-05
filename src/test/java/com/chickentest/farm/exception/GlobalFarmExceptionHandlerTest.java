package com.chickentest.farm.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.context.request.ServletWebRequest;

class GlobalFarmExceptionHandlerTest {

  private final GlobalFarmExceptionHandler handler = new GlobalFarmExceptionHandler();
  private final ServletWebRequest request =
      new ServletWebRequest(new MockHttpServletRequest("GET", "/farm/test"));

  @Test
  void catchAll_returnsGenericMessage_withoutInternalDetail() {
    ResponseEntity<String> response =
        handler.handleGlobalException(new RuntimeException("SELECT * FROM users"), request);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertEquals(
        "An unexpected internal server error occurred. Please try again later.",
        response.getBody());
    assertFalse(response.getBody().contains("SELECT"));
  }

  @Test
  void accessDenied_returns403_withoutPolicyDetail() {
    ResponseEntity<String> response =
        handler.handleAccessDenied(new AccessDeniedException("policy X"), request);

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    assertEquals("Access denied", response.getBody());
  }
}
