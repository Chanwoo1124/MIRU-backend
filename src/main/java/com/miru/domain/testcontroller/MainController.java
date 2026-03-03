package com.miru.domain.testcontroller;

import com.miru.global.auth.dto.CustomOAuth2User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class MainController {

    @GetMapping("/")
    @ResponseBody
    public String main(@AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        if (oAuth2User == null) {
            return "로그인 안 된 상태입니다.";
        }

        return "로그인 성공! <br>" +
                "ID (PK): " + oAuth2User.getName() + "<br>" +
                "Role: " + oAuth2User.getAuthorities() + "<br>" +
                "Status: " + oAuth2User.getStatus();
    }
}