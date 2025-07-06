package com.chickentest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserMe {
    private Long id;
    private String username;
    private String role;
    private double balance;
}
