package com.jamil.ahadith.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class VerificationPageController {

    @GetMapping("/verify-email")
    public String verifyEmailPage() {
        return "forward:/verify-email.html";
    }
}
