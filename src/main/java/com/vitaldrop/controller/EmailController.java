package com.vitaldrop.controller;

import com.vitaldrop.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/email")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @GetMapping("/test")
    public String sendTestMail() {

        emailService.sendEmail(
                "nm197374@gmail.com",
                "Vital Drop Test",
                "Congratulations! Gmail integration is working."
        );

        return "Email Sent Successfully";
    }
}