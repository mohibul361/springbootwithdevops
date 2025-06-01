package com.example.SpringBootWithSecurityAndDevops.dto;

import com.example.SpringBootWithSecurityAndDevops.enums.Gender;
import com.example.SpringBootWithSecurityAndDevops.enums.Role;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdatedRequest {

    private Long id;
    private String name;
    private String email;
    private String password;
    private String confirmPassword;
    private Gender gender;
    private String mobileNumber;
    private String organization;
    private Role role;
}