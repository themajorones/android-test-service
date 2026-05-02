package dev.themajorones.autotest.service;

import org.springframework.stereotype.Service;
import dev.themajorones.autotest.entity.User;
import dev.themajorones.autotest.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Save or update user in database based on GitHub OAuth data
     */
    public User saveOrUpdateUser(String githubId, String username, String email, String avatarUrl) {
        var existingUser = userRepository.findByGithubId(githubId);

        if (existingUser.isPresent()) {
            // Update existing user
            User user = existingUser.get();
            user.setUsername(username);
            return userRepository.save(user);
        } else {
            // Create new user
            User newUser = new User();
            newUser.setGithubId(githubId);
            newUser.setUsername(username);
            return userRepository.save(newUser);
        }
    }

    /**
     * Find user by GitHub ID
     */
    public User findByGithubId(String githubId) {
        return userRepository.findByGithubId(githubId).orElse(null);
    }

    /**
     * Find user by username
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }
}
