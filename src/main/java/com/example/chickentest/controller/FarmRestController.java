package com.example.chickentest.controller;

import com.example.chickentest.dto.CreateFarmRequest;
import com.example.chickentest.entity.Farm;
import com.example.chickentest.entity.User;
import com.example.chickentest.service.FarmService;
import com.example.chickentest.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/farms")
public class FarmRestController {

    @Autowired
    private FarmService farmService;

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<Farm> createFarm(@RequestBody CreateFarmRequest req, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found")); // Should not happen if authenticated
        Farm farm = farmService.createFarm(req.getName(), user);
        return new ResponseEntity<>(farm, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Farm>> getMyFarms(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<Farm> farms = farmService.getFarmsByOwner(user);
        return ResponseEntity.ok(farms);
    }
}
