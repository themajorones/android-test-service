package dev.themajorones.autotest.service;

import java.time.Instant;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import dev.themajorones.autotest.repository.AuthenticationRepository;
import dev.themajorones.autotest.repository.ImageRepository;
import dev.themajorones.autotest.repository.UserRepository;
import dev.themajorones.models.entity.Authentication;
import dev.themajorones.models.entity.Image;
import dev.themajorones.models.entity.User;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
    private final AuthenticationRepository authenticationRepository;

    public UserService(
            UserRepository userRepository,
            ImageRepository imageRepository,
            AuthenticationRepository authenticationRepository
    ) {
        this.userRepository = userRepository;
        this.imageRepository = imageRepository;
        this.authenticationRepository = authenticationRepository;
    }

    @Transactional
    public User saveOrUpdateUser(
            String githubId,
            String username,
            String avatarUrl,
            String accessToken,
            Instant accessTokenExpiresAt,
            String refreshToken,
            Instant refreshTokenExpiresAt
    ) {
        var existingUser = userRepository.findByGithubId(githubId);
        Image avatar = saveOrFindAvatar(avatarUrl);
        Authentication savedAccessToken = saveAuthentication(accessToken, accessTokenExpiresAt);
        Authentication savedRefreshToken = saveAuthentication(
            refreshToken != null ? refreshToken : "",
            refreshTokenExpiresAt != null ? refreshTokenExpiresAt : accessTokenExpiresAt
        );

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            user.setUsername(username);
            user.setAvatar(avatar);
            user.setAccessToken(savedAccessToken);
            user.setRefreshToken(savedRefreshToken);
            return userRepository.save(user);
        } else {
            User newUser = new User();
            newUser.setGithubId(githubId);
            newUser.setUsername(username);
            newUser.setAvatar(avatar);
            newUser.setAccessToken(savedAccessToken);
            newUser.setRefreshToken(savedRefreshToken);
            return userRepository.save(newUser);
        }
    }

    private Image saveOrFindAvatar(String avatarUrl) {
        return imageRepository.findByUrl(avatarUrl)
            .orElseGet(() -> {
                Image image = new Image();
                image.setUrl(avatarUrl);
                return imageRepository.save(image);
            });
    }

    private Authentication saveAuthentication(String token, Instant expiresAt) {
        Authentication authentication = new Authentication();
        authentication.setContent(token);
        authentication.setExpireAt(expiresAt != null ? expiresAt.toEpochMilli() : 0L);
        return authenticationRepository.save(authentication);
    }

}
