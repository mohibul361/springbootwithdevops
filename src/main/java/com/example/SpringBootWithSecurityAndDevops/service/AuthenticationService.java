package com.example.SpringBootWithSecurityAndDevops.service;

import com.example.SpringBootWithSecurityAndDevops.dto.AuthenticationRequest;
import com.example.SpringBootWithSecurityAndDevops.dto.AuthenticationResponse;
import com.example.SpringBootWithSecurityAndDevops.dto.RegisterRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

public interface AuthenticationService {

    ResponseEntity<String> register(RegisterRequest request);

    AuthenticationResponse authenticate(AuthenticationRequest request);

    void sendPasswordResetLink(String email);

    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException;
}
