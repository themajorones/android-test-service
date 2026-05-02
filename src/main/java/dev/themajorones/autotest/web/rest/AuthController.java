package dev.themajorones.autotest.web.rest;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @GetMapping("/success")
    public String success(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            String username = oAuth2User.getAttribute("login");
            return "GitHub login successful for user: " + username;
        }
        return "GitHub login successful";
    }

    @GetMapping("/failure")
    public String failure(String error) {
        return "GitHub login failed: " + (error != null ? error : "Unknown error");
    }

    @GetMapping("/info")
    public Object userInfo(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            return oAuth2User.getAttributes();
        }
        return "Not authenticated";
    }
}
