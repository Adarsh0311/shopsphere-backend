package com.shopsphere.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtAuthResponse {
    private String accessToken;
    private  String tokenType = "Bearer";
    private String userId;
    private String username;
    private String email;
    // Optionally include roles, etc.
}