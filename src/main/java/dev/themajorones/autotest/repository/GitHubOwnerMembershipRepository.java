package dev.themajorones.autotest.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import dev.themajorones.models.entity.GitHubOwnerMembership;
import dev.themajorones.models.entity.GitHubUser;

@Repository
public interface GitHubOwnerMembershipRepository extends JpaRepository<GitHubOwnerMembership, Integer> {

    List<GitHubOwnerMembership> findAllByUser(GitHubUser user);

    void deleteAllByUser(GitHubUser user);
}
