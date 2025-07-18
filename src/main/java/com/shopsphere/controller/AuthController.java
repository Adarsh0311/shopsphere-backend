package com.shopsphere.controller;

import com.shopsphere.dto.JwtAuthResponse;
import com.shopsphere.dto.LoginRequest;
import com.shopsphere.dto.RegisterRequest;
import com.shopsphere.model.User;
import com.shopsphere.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService  userService;

    /**
     * POST /api/auth/register : Register a new user.
     * @param registerRequest The registration request DTO.
     * @return ResponseEntity with the registered user's details (or DTO) and HTTP status 201 Created.
     */
    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody RegisterRequest registerRequest) {
        User registeredUser = userService.registerUser(registerRequest);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/users/{id}")
                .buildAndExpand(registeredUser.getUserId())
                .toUri();
        return ResponseEntity.created(location).body(registeredUser);
    }

    /**
     * POST /api/auth/login: Authenticate user and return JWT.
     * @param loginRequest The login request DTO.
     * @return ResponseEntity with {@link JwtAuthResponse} and HTTP status 200 OK.
     */
    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponse> login(@RequestBody LoginRequest loginRequest) {
        JwtAuthResponse authResponse = userService.login(loginRequest);
        return ResponseEntity.ok(authResponse);

    }

}