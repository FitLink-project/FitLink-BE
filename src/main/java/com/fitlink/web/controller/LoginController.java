package com.fitlink.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
@RequiredArgsConstructor
public class LoginController {

    @Value("${frontend.base-url:https://fit-link-fe.vercel.app}")
    private String frontendBaseUrl;

    @GetMapping("/login")
    public void loginPage(HttpServletResponse response) throws IOException {
        // 프론트엔드 로그인 페이지로 리다이렉트
        String frontendLoginUrl = frontendBaseUrl + "/login";
        response.sendRedirect(frontendLoginUrl);
    }
}

