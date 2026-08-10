package com.itop.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {

    private String token;
    private String type = "Bearer";
    private String username;
    private String email;
    private Long userId;

    public JwtResponse(String token, String username, String email, Long userId) {
        this.token = token;
        this.username = username;
        this.email = email;
        this.userId = userId;
    }
}