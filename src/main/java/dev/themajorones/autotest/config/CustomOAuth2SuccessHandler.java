package dev.themajorones.autotest.config;

import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import dev.themajorones.autotest.repository.AuthenticationRepository;
import dev.themajorones.autotest.repository.ImageRepository;
import dev.themajorones.autotest.repository.UserRepository;
import dev.themajorones.models.entity.Image;
import dev.themajorones.models.entity.GitHubAuthentication;
import dev.themajorones.models.entity.GitHubUser;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
    private final AuthenticationRepository authenticationRepository;
    private final OAuth2AuthorizedClientService authorizedClientService;

    public CustomOAuth2SuccessHandler(
            UserRepository userRepository,
            ImageRepository imageRepository,
            AuthenticationRepository authenticationRepository,
            OAuth2AuthorizedClientService authorizedClientService
    ) {
        this.userRepository = userRepository;
        this.imageRepository = imageRepository;
        this.authenticationRepository = authenticationRepository;
        this.authorizedClientService = authorizedClientService;
    }

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        
        String githubId = oAuth2User.getAttribute("id").toString();
        String username = oAuth2User.getAttribute("login").toString();
        String avatarUrl = oAuth2User.getAttribute("avatar_url") != null ? oAuth2User.getAttribute("avatar_url").toString() : "";
        OAuth2AuthorizedClient authorizedClient = authorizedClient(authentication);
        String accessToken = authorizedClient.getAccessToken().getTokenValue();
        var accessTokenExpiresAt = authorizedClient.getAccessToken().getExpiresAt();
        String refreshToken = authorizedClient.getRefreshToken() != null ? authorizedClient.getRefreshToken().getTokenValue() : null;
        var refreshTokenExpiresAt = authorizedClient.getRefreshToken() != null ? authorizedClient.getRefreshToken().getExpiresAt() : null;

        Image avatar = imageRepository.findByUrl(avatarUrl).orElseGet(() -> imageRepository.save(new Image().setUrl(avatarUrl)));
        GitHubAuthentication savedAccessToken = authenticationRepository.save(new GitHubAuthentication(accessToken, accessTokenExpiresAt != null ? accessTokenExpiresAt.toEpochMilli() : null));
        GitHubAuthentication savedRefreshToken = refreshToken != null ? authenticationRepository.save(new GitHubAuthentication(refreshToken, refreshTokenExpiresAt != null ? refreshTokenExpiresAt.toEpochMilli() : null)) : null;

        GitHubUser user = userRepository.findByGithubId(githubId)
            .orElseGet(GitHubUser::new)
            .setGithubId(githubId)
            .setUsername(username)
            .setAvatar(avatar)
            .setAccessToken(savedAccessToken)
            .setRefreshToken(savedRefreshToken);

        userRepository.save(user);
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
