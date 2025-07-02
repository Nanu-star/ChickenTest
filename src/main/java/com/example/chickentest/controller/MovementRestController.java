package com.example.chickentest.controller;

import com.example.chickentest.entity.Movement;
import com.example.chickentest.entity.User;
import com.example.chickentest.service.MovementService;
import com.example.chickentest.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/farms/{farmId}/articles/{articleId}")
public class MovementRestController {

    @Autowired
    private MovementService movementService;

    @Autowired
    private UserService userService;

    @PostMapping("/buy")
    public ResponseEntity<?> buyArticle(
            @PathVariable Long farmId,
            @PathVariable Long articleId,
            @RequestParam int quantity,
            @RequestParam double price, // Price could also be fetched from Article entity
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<Movement> movementOpt = movementService.recordBuyMovement(articleId, quantity, price, user, farmId);

        return movementOpt.map(movement -> new ResponseEntity<>(movement, HttpStatus.CREATED))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Error processing buy operation. Check farm/article ID, stock, or ownership."));
    }

    @PostMapping("/sell")
    public ResponseEntity<?> sellArticle(
            @PathVariable Long farmId,
            @PathVariable Long articleId,
            @RequestParam int quantity,
            @RequestParam double price, // Price could also be fetched from Article entity
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<Movement> movementOpt = movementService.recordSellMovement(articleId, quantity, price, user, farmId);

        return movementOpt.map(movement -> new ResponseEntity<>(movement, HttpStatus.CREATED))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Error processing sell operation. Check farm/article ID, stock, or ownership."));
    }
}
