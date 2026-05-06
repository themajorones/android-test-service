package dev.themajorones.autotest.service;

import java.time.Instant;
import java.util.Optional;
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
    public User saveOrUpdateUser(User user) {
        Optional<User> existingUser = userRepository.findByGithubId(user.getGithubId());
        Image avatar = saveOrFindAvatar(user.getAvatar().getUrl());
        Authentication savedAccessToken = saveAuthentication(user.getAccessToken().getContent(), Instant.ofEpochMilli(user.getAccessToken().getExpireAt()));
        Authentication savedRefreshToken = saveAuthentication(
            user.getRefreshToken() != null ? user.getRefreshToken().getContent() : "",
            user.getRefreshToken() != null ? Instant.ofEpochMilli(user.getRefreshToken().getExpireAt()) : Instant.ofEpochMilli(user.getAccessToken().getExpireAt())
        );

        User memoryUser = new User();

        if (existingUser.isPresent()) {
            memoryUser = existingUser.get();
            memoryUser.setUsername(user.getUsername());
            memoryUser.setAvatar(avatar);
            memoryUser.setAccessToken(savedAccessToken);
            memoryUser.setRefreshToken(savedRefreshToken);
        } else {
            memoryUser = new User();
            memoryUser.setGithubId(user.getGithubId());
            memoryUser.setUsername(user.getUsername());
            memoryUser.setAvatar(avatar);
            memoryUser.setAccessToken(savedAccessToken);
            memoryUser.setRefreshToken(savedRefreshToken);
        }
        return userRepository.save(memoryUser);
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
