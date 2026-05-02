package dev.themajorones.autotest.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import dev.themajorones.autotest.repository.UserRepository;
import dev.themajorones.autotest.service.UserService;

@Component
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final UserRepository userRepository;
    private final UserService userService;

    public CustomOAuth2SuccessHandler(
            OAuth2AuthorizedClientService authorizedClientService,
            UserRepository userRepository,
            UserService userService
    ) {
        this.authorizedClientService = authorizedClientService;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        // Get OAuth2User details from GitHub
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        
        // Extract GitHub user info
        String githubId = oAuth2User.getAttribute("id").toString();
        String username = oAuth2User.getAttribute("login").toString();
        String email = oAuth2User.getAttribute("email") != null ? 
                       oAuth2User.getAttribute("email").toString() : "";
        String avatarUrl = oAuth2User.getAttribute("avatar_url") != null ? 
                           oAuth2User.getAttribute("avatar_url").toString() : "";

        // Save or update user in database
        userService.saveOrUpdateUser(githubId, username, email, avatarUrl);

        // Redirect to frontend or dashboard
        response.sendRedirect("/auth/success");
    }
}
