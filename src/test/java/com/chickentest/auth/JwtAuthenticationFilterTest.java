package com.chickentest.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.chickentest.config.AppSettings;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

class JwtAuthenticationFilterTest {

  private static final String SECRET =
      "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

  private JwtService jwtService;
  private UserDetailsService userDetailsService;
  private JwtAuthenticationFilter filter;

  @BeforeEach
  void setUp() {
    jwtService = new JwtService(settings(86400000L));
    userDetailsService = mock(UserDetailsService.class);
    filter = new JwtAuthenticationFilter(jwtService, userDetailsService);
  }

  @AfterEach
  void clearContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void malformedToken_continuesChain_withoutAuthentication() throws Exception {
    MockHttpServletRequest request = requestWithToken("abc.def.ghi");
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain chain = mock(FilterChain.class);

    assertDoesNotThrow(() -> filter.doFilter(request, response, chain));

    verify(chain).doFilter(request, response);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void expiredToken_continuesChain_withoutAuthentication() throws Exception {
    JwtService expiredService = new JwtService(settings(-1000L));
    String expired = expiredService.generateToken(user("testuser"));
    MockHttpServletRequest request = requestWithToken(expired);
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain chain = mock(FilterChain.class);

    assertDoesNotThrow(() -> filter.doFilter(request, response, chain));

    verify(chain).doFilter(request, response);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void validTokenForUnknownUser_continuesChain_withoutAuthentication() throws Exception {
    String token = jwtService.generateToken(user("ghost"));
    when(userDetailsService.loadUserByUsername(anyString()))
        .thenThrow(new UsernameNotFoundException("no such user"));
    MockHttpServletRequest request = requestWithToken(token);
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain chain = mock(FilterChain.class);

    assertDoesNotThrow(() -> filter.doFilter(request, response, chain));

    verify(chain).doFilter(request, response);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  private static AppSettings settings(long expiration) {
    AppSettings settings = new AppSettings();
    settings.setJwt(new AppSettings.Jwt(SECRET, expiration));
    return settings;
  }

  private static UserDetails user(String username) {
    return User.withUsername(username).password("x").roles("USER").build();
  }

  private static MockHttpServletRequest requestWithToken(String token) {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/farm/articles");
    request.addHeader("Authorization", "Bearer " + token);
    return request;
  }
}
