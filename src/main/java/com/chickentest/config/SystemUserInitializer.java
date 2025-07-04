package com.chickentest.config;

import org.springframework.stereotype.Component;

import com.chickentest.domain.User;
import com.chickentest.repository.UserRepository;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Component
public class SystemUserInitializer {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @PostConstruct
    public void createSystemUser() {
        userRepository.findByUsername("system").orElseGet(() -> {
            User systemUser = new User();
            systemUser.setUsername("system");
            systemUser.setPassword(passwordEncoder.encode("admin"));
            systemUser.setBalance(0);
            systemUser.setRole("SYSTEM");
            return userRepository.save(systemUser);
        });
    }
}