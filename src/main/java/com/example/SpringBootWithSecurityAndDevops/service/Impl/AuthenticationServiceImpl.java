package com.example.SpringBootWithSecurityAndDevops.service.Impl;

import com.example.SpringBootWithSecurityAndDevops.Config.JwtService;
import com.example.SpringBootWithSecurityAndDevops.dto.AuthenticationRequest;
import com.example.SpringBootWithSecurityAndDevops.dto.AuthenticationResponse;
import com.example.SpringBootWithSecurityAndDevops.dto.RegisterRequest;
import com.example.SpringBootWithSecurityAndDevops.entity.Token;
import com.example.SpringBootWithSecurityAndDevops.entity.User;
import com.example.SpringBootWithSecurityAndDevops.enums.TokenType;
import com.example.SpringBootWithSecurityAndDevops.repository.TokenRepository;
import com.example.SpringBootWithSecurityAndDevops.repository.UserRepository;
import com.example.SpringBootWithSecurityAndDevops.service.AuthenticationService;
import com.example.SpringBootWithSecurityAndDevops.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final EmailService emailService;

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private final AuthenticationManager authenticationManager;

    public ResponseEntity<String> register(RegisterRequest request) {
        {

            String rawPassword = request.getPassword();

            if (rawPassword == null || rawPassword.isEmpty() || rawPassword.isBlank()) {
                throw new IllegalArgumentException("Password cannot be null or empty or blank");
            }


            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Email already exists");
            }

            if(userRepository.existsByMobileNumber(request.getMobileNumber())){
                throw new IllegalArgumentException("Mobile number already exist!");
            }

            String encodedPassword = passwordEncoder.encode(rawPassword);

            User newUser = new User();
            newUser.setName(request.getName());
            newUser.setEmail(request.getEmail());
            newUser.setPassword(encodedPassword);
            newUser.setGender(request.getGender());
            newUser.setMobileNumber(request.getMobileNumber());
            newUser.setOrganization(request.getOrganization());
            newUser.setRole(request.getRole());

            var savedUser = userRepository.save(newUser);
//            var jwtToken = jwtService.generateToken(newUser);
//            var refreshToken = jwtService.generateRefreshToken(newUser);
//
//            saveUserToken(savedUser, jwtToken);

            userRepository.save(newUser);

            return ResponseEntity.ok("User registered successsfully");

        }
    }


    public AuthenticationResponse authenticate(AuthenticationRequest request){

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with this email"));

        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        revokeAllUserTokens(user);
        saveUserToken(user, jwtToken);

        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public void sendPasswordResetLink(String email) {

        Optional<User> userOptional = userRepository.findByEmail(email);

        if(userOptional.isEmpty()){
            throw new IllegalArgumentException("User with this email does not exist!");
        }

        User user = userOptional.get();
        String resetToken = UUID.randomUUID().toString();

        String resetLink = "http://localhost:8080/reset-password?token=" + resetToken;

        emailService.sendPasswordResetEmail(user.getEmail(), resetLink);

    }

    private void saveUserToken(User user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }


    private void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }

    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String userEmail;
        if (authHeader == null ||!authHeader.startsWith("Bearer ")) {
            return;
        }
        refreshToken = authHeader.substring(7);
        userEmail = jwtService.extractUsername(refreshToken);
        if (userEmail != null) {
            var user = this.userRepository.findByEmail(userEmail)
                    .orElseThrow();
            if (jwtService.isTokenValid(refreshToken, user)) {
                var accessToken = jwtService.generateToken(user);
                revokeAllUserTokens(user);
                saveUserToken(user, accessToken);
                var authResponse = AuthenticationResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build();
                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
            }
        }
    }

}

