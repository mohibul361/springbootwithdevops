package com.example.SpringBootWithSecurityAndDevops.service;

import com.example.SpringBootWithSecurityAndDevops.Config.JwtService;
import com.example.SpringBootWithSecurityAndDevops.entity.User;
import com.example.SpringBootWithSecurityAndDevops.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.*;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class PermissionService {

    private final UserRepository userRepository;

    private final JwtService jwtService;

    private final HttpServletRequest httpServletRequest;

    public boolean hasPermissionForAdmin() {
        return hasPermission("ADMIN");
    }

    public boolean hasPermissionForManager() {
        return hasPermission("MANAGER");
    }

    public boolean hasPermissionForAdminOrManager() {
        return hasPermission("ADMIN") || hasPermission("MANAGER");
    }

    private boolean hasPermission(String requiredRole) {

        String token = httpServletRequest.getHeader("Authorization");
        String email;
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            email = jwtService.extractUsername(token);
        } else {
            return false;
        }


        Optional<User> user = userRepository.findByEmail(email);

        return user.map(u -> u.getRole().name().equalsIgnoreCase(requiredRole))
                .orElse(false);
    }

}
