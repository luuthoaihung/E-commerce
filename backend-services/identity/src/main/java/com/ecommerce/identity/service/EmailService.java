package com.ecommerce.identity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    

    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Mã OTP khôi phục mật khẩu - E-commerce");
        message.setText("Mã OTP của bạn là: " + otp + "\nMã này có hiệu lực trong vòng 5 phút.");
        mailSender.send(message);
    }
}