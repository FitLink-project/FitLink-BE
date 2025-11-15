package com.fitlink.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class LoginController {

    @Value("${server.base-url:https://www.fitlink1207.store}")
    private String baseUrl;

    @GetMapping("/login")
    public String loginPage(Model model) {
        // 절대 경로로 OAuth2 인증 URL 설정
        model.addAttribute("googleAuthUrl", baseUrl + "/oauth2/authorization/google");
        model.addAttribute("kakaoAuthUrl", baseUrl + "/oauth2/authorization/kakao");
        return "login";
    }
}

