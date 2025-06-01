package com.example.SpringBootWithSecurityAndDevops.controller;

import com.example.SpringBootWithSecurityAndDevops.dto.UserProfileDTO;
import com.example.SpringBootWithSecurityAndDevops.dto.UserUpdatedRequest;
import com.example.SpringBootWithSecurityAndDevops.entity.User;
import com.example.SpringBootWithSecurityAndDevops.repository.UserRepository;
import com.example.SpringBootWithSecurityAndDevops.service.PermissionService;
import com.example.SpringBootWithSecurityAndDevops.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;
    private final PermissionService permissionService;
    private final UserRepository userRepository;



//    @GetMapping("/profile")
//    public ResponseEntity<User> getUserProfile(@AuthenticationPrincipal UserDetailsDTO userDetails) {
//        String email = userDetails.getEmail();
//        return userService.getUserByEmail(email);
//    }

    @PutMapping("/update/profile/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<String> updateProfile(
            @PathVariable(required = false) Long id,
            @RequestBody UserUpdatedRequest userUpdatedRequest,
            Authentication authentication) {

        String currentUsername = authentication.getName();
        Long currentUserId = getCurrentUserId(currentUsername);

        // If id is null, update the current user's profile
        if (id == null) {
            id = currentUserId;
        }

        // Check admin permissions
        if (currentUserIsAdmin(authentication)) {
            return userService.updateProfile(id, userUpdatedRequest);
        }

        // Check manager permissions
        if (currentUserIsManager(authentication)) {
            if (!id.equals(currentUserId)) {
                // Managers can only update their own profile
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Managers are not allowed to update other users' profiles.");
            }
            return userService.updateProfile(id, userUpdatedRequest);
        }

        // If no valid role, deny access
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("You do not have permission to update this profile.");
    }

    private boolean currentUserIsAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }

    private boolean currentUserIsManager(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_MANAGER"));
    }

    private Long getCurrentUserId(String email) {

        Optional<User> user = userRepository.findByEmail(email);
        return user.get().getId();
    }


    @GetMapping("/profile/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public UserProfileDTO getProfile(@RequestParam(required = false) Long id, Principal principal) {

        if (id == null) {
            // Assuming 'principal' contains information to fetch the logged-in user's ID
            id = userService.getLoggedInUserId(principal.getName()); // Implement this in the service layer
        }

        // Call the service layer to fetch the user profile
        return userService.getProfile(id);
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllUsers( @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "2") int size) {
        return userService.getAllUser(page, size);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error: " + e.getMessage());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserProfileDTO> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
