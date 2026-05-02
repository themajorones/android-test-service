package dev.themajorones.autotest.config;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import dev.themajorones.autotest.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private final UserService userService;
    private final OAuth2AuthorizedClientService authorizedClientService;

    public CustomOAuth2SuccessHandler(
            UserService userService,
            OAuth2AuthorizedClientService authorizedClientService
    ) {
        this.userService = userService;
        this.authorizedClientService = authorizedClientService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        // Get OAuth2User details from GitHub
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        
        // Extract GitHub user info
        String githubId = oAuth2User.getAttribute("id").toString();
        String username = oAuth2User.getAttribute("login").toString();
        String avatarUrl = oAuth2User.getAttribute("avatar_url") != null ? oAuth2User.getAttribute("avatar_url").toString() : "";
        OAuth2AuthorizedClient authorizedClient = authorizedClient(authentication);
        String accessToken = authorizedClient.getAccessToken().getTokenValue();
        var accessTokenExpiresAt = authorizedClient.getAccessToken().getExpiresAt();
        String refreshToken = authorizedClient.getRefreshToken() != null
            ? authorizedClient.getRefreshToken().getTokenValue()
            : null;
        var refreshTokenExpiresAt = authorizedClient.getRefreshToken() != null
            ? authorizedClient.getRefreshToken().getExpiresAt()
            : null;

        // Save or update user in database
        userService.saveOrUpdateUser(
            githubId,
            username,
            avatarUrl,
            accessToken,
            accessTokenExpiresAt,
            refreshToken,
            refreshTokenExpiresAt
        );

        // Redirect to frontend or dashboard
        response.sendRedirect("/");
    }

    private OAuth2AuthorizedClient authorizedClient(Authentication authentication) {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        return authorizedClientService.loadAuthorizedClient(
            oauthToken.getAuthorizedClientRegistrationId(),
            oauthToken.getName()
        );
    }
}
