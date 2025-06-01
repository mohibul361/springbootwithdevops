package com.example.SpringBootWithSecurityAndDevops.service;

import com.example.SpringBootWithSecurityAndDevops.dto.UserProfileDTO;
import com.example.SpringBootWithSecurityAndDevops.dto.UserUpdatedRequest;
import com.example.SpringBootWithSecurityAndDevops.entity.User;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

public interface UserService {

    ResponseEntity<User> getUserByEmail(String email);

    ResponseEntity<?> getAllUser(int page, int size);

    UserProfileDTO getProfile(Long id);

    ResponseEntity<String> updateProfile(Long id, UserUpdatedRequest userUpdatedRequest);

    Optional<UserProfileDTO> getUserById(Long id);


    Long getLoggedInUserId(String name);
}
