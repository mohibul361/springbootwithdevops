package com.example.SpringBootWithSecurityAndDevops.service.Impl;

import com.example.SpringBootWithSecurityAndDevops.dto.UserProfileDTO;
import com.example.SpringBootWithSecurityAndDevops.dto.UserResponseModel;
import com.example.SpringBootWithSecurityAndDevops.dto.UserUpdatedRequest;
import com.example.SpringBootWithSecurityAndDevops.entity.User;
import com.example.SpringBootWithSecurityAndDevops.enums.Role;
import com.example.SpringBootWithSecurityAndDevops.exception.ResourceNotFoundException;
import com.example.SpringBootWithSecurityAndDevops.repository.UserRepository;
import com.example.SpringBootWithSecurityAndDevops.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public ResponseEntity<User> getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return ResponseEntity.ok(user);
    }

    public ResponseEntity<String> updateProfile(Long id, UserUpdatedRequest userUpdatedRequest) {
        try {
            // Get the authenticated user's email
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentEmail = authentication.getName();
            log.info("Authenticated user email: {}", currentEmail);

            User authenticatedUser = userRepository.findByEmail(currentEmail)
                    .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

            User user = userRepository.findByEmail(userUpdatedRequest.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found or unauthorized"));

            log.info("Authenticated user role: {}", authenticatedUser.getRole());
            log.info("Requested update for user email: {}", userUpdatedRequest.getEmail());

            if (authenticatedUser.getRole() == Role.ADMIN || authenticatedUser.getId().equals(user.getId())) {
                user.setName(userUpdatedRequest.getName());
                if (userUpdatedRequest.getPassword() != null && !userUpdatedRequest.getPassword().isEmpty()) {
                    user.setPassword(passwordEncoder.encode(userUpdatedRequest.getPassword()));
                }
                user.setEmail(userUpdatedRequest.getEmail());
                user.setGender(userUpdatedRequest.getGender());
                user.setMobileNumber(userUpdatedRequest.getMobileNumber());
                user.setOrganization(userUpdatedRequest.getOrganization());

                if (authenticatedUser.getRole() == Role.ADMIN) {
                    user.setRole(userUpdatedRequest.getRole());
                }

                userRepository.save(user);
                return ResponseEntity.ok("User updated successfully.");
            } else if (authenticatedUser.getRole() == Role.MANAGER && authenticatedUser.getId().equals(user.getId())) {
                user.setName(userUpdatedRequest.getName());
                if (userUpdatedRequest.getPassword() != null && !userUpdatedRequest.getPassword().isEmpty()) {
                    user.setPassword(passwordEncoder.encode(userUpdatedRequest.getPassword()));
                }
                user.setEmail(userUpdatedRequest.getEmail());
                user.setGender(userUpdatedRequest.getGender());
                user.setMobileNumber(userUpdatedRequest.getMobileNumber());
                user.setOrganization(userUpdatedRequest.getOrganization());

                userRepository.save(user);
                return ResponseEntity.ok("Profile updated successfully.");
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to update this profile.");
            }
        } catch (Exception e) {
            log.error("Error updating profile", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> getAllUser(int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<User> userPage = userRepository.findAll(pageable);

            if (userPage.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("There are no users registered right now.");
            }

            List<UserResponseModel> responseList = userPage.getContent().stream().map(user ->
                    UserResponseModel.builder()
                            .id(user.getId())
                            .name(user.getName())
                            .email(user.getEmail())
                            .mobileNumber(user.getMobileNumber())
                            .organization(user.getOrganization())
                            .gender(user.getGender())
                            .role(user.getRole())
                            .build()
            ).collect(Collectors.toList());

            // Wrap the converted list in a Page object
            Page<UserResponseModel> responsePage = new PageImpl<>(responseList, pageable, userPage.getTotalElements());

            return ResponseEntity.ok(responsePage);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while fetching users.");
        }
    }

    @Override
    public UserProfileDTO getProfile(Long id) {
        User user;

        if (id == null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName(); // Fetch logged-in user's email

            // Fetch user by email
            user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Logged-in user not found"));
        } else {
            // Fetch user by id
            user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        }

        UserProfileDTO userProfileDTO = new UserProfileDTO();
        userProfileDTO.setId(user.getId());
        userProfileDTO.setName(user.getName());
        userProfileDTO.setEmail(user.getEmail());
        userProfileDTO.setMobileNumber(user.getMobileNumber());
        userProfileDTO.setOrganization(user.getOrganization());
        userProfileDTO.setRole(String.valueOf(user.getRole()));
        userProfileDTO.setGender(String.valueOf(user.getGender()));

        return userProfileDTO;
    }


    @Override
    public Optional<UserProfileDTO> getUserById(Long id) {
        return userRepository.findById(id)
                .map(user -> UserProfileDTO.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .email(user.getEmail())
                        .mobileNumber(user.getMobileNumber())
                        .organization(user.getOrganization())
                        .role(String.valueOf(user.getRole()))
                        .gender(String.valueOf(user.getGender()))
                        .build()
                );
    }

    public Long getLoggedInUserId(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        return user.getId();
    }

}