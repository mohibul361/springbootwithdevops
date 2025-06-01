package com.example.SpringBootWithSecurityAndDevops.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileDTO {

    private Long id;
    private String name;
    private String email;
    private String mobileNumber;
    private String organization;
    private String role;
    private String gender;


}
