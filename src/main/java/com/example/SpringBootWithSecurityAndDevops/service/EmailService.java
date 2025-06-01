package com.example.SpringBootWithSecurityAndDevops.service;

public interface EmailService {

    void sendPasswordResetEmail(String to, String resetLink);
}
