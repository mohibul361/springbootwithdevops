package com.example.SpringBootWithSecurityAndDevops.dto;

import com.example.SpringBootWithSecurityAndDevops.enums.Gender;
import com.example.SpringBootWithSecurityAndDevops.enums.Role;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseModel {

    private long id;
    private String name;
    private String email;
    private String mobileNumber;
    private String organization;
    private Gender gender;
    private Role role;
}
