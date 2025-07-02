package com.chickentest.controller;

import com.chickentest.config.JwtService;
import com.chickentest.domain.User;
import com.chickentest.repository.UserRepository;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            Optional<User> existingUser = userRepository.findByUsername(user.getUsername());
            if (existingUser.isPresent()) {
                return ResponseEntity.badRequest().body("Username already exists");
            }
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userRepository.save(user);
            return ResponseEntity.ok("Registration successful");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Registration failed: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginRequest, HttpServletRequest request) {
         try {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(),
                loginRequest.getPassword())
        );
        // Usuario autenticado, generamos el token
        UserDetails user = (UserDetails) authentication.getPrincipal();
        String jwt = jwtService.generateToken(user);

        // Podés devolver solo el token, o más info si querés
        return ResponseEntity.ok(Map.of(
            "token", jwt,
            "username", user.getUsername(),
            "roles", user.getAuthorities()
        ));
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
    }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication, HttpServletRequest request) {
        System.out.println("[DEBUG] /auth/me called");
        if (authentication == null) {
            System.out.println("[DEBUG] authentication is null");
        } else {
            System.out.println("[DEBUG] authentication: " + authentication);
            System.out.println("[DEBUG] principal: " + authentication.getPrincipal());
            System.out.println("[DEBUG] authorities: " + authentication.getAuthorities());
        }
        // Try to log CSRF token if present
        Object csrf = request.getAttribute("org.springframework.security.web.csrf.CsrfToken");
        if (csrf != null) {
            System.out.println("[DEBUG] CSRF token: " + csrf.toString());
        } else {
            System.out.println("[DEBUG] No CSRF token found in request attributes");
        }
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            User user = (User) authentication.getPrincipal();
            user.setPassword(null);
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No authenticated user");
    }
}
