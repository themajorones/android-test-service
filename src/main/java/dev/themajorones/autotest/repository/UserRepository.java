package dev.themajorones.autotest.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import dev.themajorones.models.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    
    Optional<User> findByGithubId(String githubId);

    Optional<User> findByUsername(String username);

}
