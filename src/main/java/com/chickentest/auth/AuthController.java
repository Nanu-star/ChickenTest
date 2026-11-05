package com.chickentest.auth;

import com.chickentest.user.User;
import com.chickentest.user.UserMe;
import com.chickentest.user.UserRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/auth")
public class AuthController {
  @Autowired private UserRepository userRepository;
  @Autowired private BCryptPasswordEncoder passwordEncoder;
  @Autowired private AuthenticationManager authenticationManager;
  @Autowired private JwtService jwtService;

  @PostMapping("/register")
  public ResponseEntity<?> register(@Valid @RequestBody AuthRequest authRequest) {
    Optional<User> existingUser = userRepository.findByUsername(authRequest.getUsername());
    if (existingUser.isPresent()) {
      return ResponseEntity.badRequest().body("Username already exists");
    }
    User user = new User();
    user.setUsername(authRequest.getUsername());
    user.setPassword(passwordEncoder.encode(authRequest.getPassword()));
    userRepository.save(user);
    return ResponseEntity.ok("Registration successful");
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@Valid @RequestBody AuthRequest loginRequest) {
    try {
      Authentication authentication =
          authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(
                  loginRequest.getUsername(), loginRequest.getPassword()));
      // Authenticated user, generate token
      UserDetails user = (UserDetails) authentication.getPrincipal();
      String jwt = jwtService.generateToken(user);

      // You can return just the token, or more info
      return ResponseEntity.ok(
          Map.of(
              "token", jwt,
              "username", user.getUsername(),
              "roles", user.getAuthorities()));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
    }
  }

  @GetMapping("/me")
  public ResponseEntity<?> getCurrentUser(Authentication authentication) {
    if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No authenticated user");
    }

    User user = (User) authentication.getPrincipal();

    UserMe dto = new UserMe(user.getId(), user.getUsername(), user.getRole(), user.getBalance());

    return ResponseEntity.ok(dto);
  }
}
