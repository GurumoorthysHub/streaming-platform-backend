package com.streamingplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private String token;
    private Long expiresIn;
    private String message;
    private boolean success;
}