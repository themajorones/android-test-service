package dev.themajorones.autotest.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import dev.themajorones.models.entity.GitHubUser;

@Repository
public interface UserRepository extends JpaRepository<GitHubUser, Integer> {
    
    Optional<GitHubUser> findByGithubId(String githubId);

    Optional<GitHubUser> findByUsername(String username);

}
