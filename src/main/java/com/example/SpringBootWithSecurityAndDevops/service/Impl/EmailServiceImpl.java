package com.example.SpringBootWithSecurityAndDevops.service.Impl;

import com.example.SpringBootWithSecurityAndDevops.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;


    @Override
    public void sendPasswordResetEmail(String to, String resetLink) {

        try{
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject("Password Reset Request");

            helper.setText(
                    "<p>Hello,</p>" +
                            "<p>You requested to reset your password. Click the link below to reset it:</p>" +
                            "<p><a href=\"" + resetLink + "\">Reset Password</a></p>" +
                            "<p>If you didn’t request a password reset, you can ignore this email.</p>",
                    true
            );

            javaMailSender.send(message);

        }catch(MessagingException e){
            throw new IllegalStateException("Failed to send email", e);

        }

    }
}
