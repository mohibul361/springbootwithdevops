package com.example.SpringBootWithSecurityAndDevops.controller;

import com.example.SpringBootWithSecurityAndDevops.dto.AuthenticationRequest;
import com.example.SpringBootWithSecurityAndDevops.dto.AuthenticationResponse;
import com.example.SpringBootWithSecurityAndDevops.dto.ForgotPasswordRequest;
import com.example.SpringBootWithSecurityAndDevops.dto.RegisterRequest;
import com.example.SpringBootWithSecurityAndDevops.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;


    @PostMapping("/register")
    public ResponseEntity<String> register(
            @RequestBody RegisterRequest request
    ) {
        authenticationService.register(request);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request){

        return  ResponseEntity.ok(authenticationService.authenticate(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        authenticationService.sendPasswordResetLink(request.getEmail());
        return ResponseEntity.ok("Password reset email sent, please check your inbox.");
    }

    @PostMapping("/refresh-token")
    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        authenticationService.refreshToken(request, response);
    }

}
