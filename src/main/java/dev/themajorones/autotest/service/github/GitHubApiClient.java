package dev.themajorones.autotest.service.github;

import java.util.List;
import dev.themajorones.autotest.dto.github.GitHubArtifactResponse;
import dev.themajorones.autotest.dto.github.GitHubOwnerResponse;
import dev.themajorones.autotest.dto.github.GitHubRepoResponse;
import dev.themajorones.autotest.dto.github.GitHubWorkflowRunResponse;

public interface GitHubApiClient {

    GitHubOwnerResponse getCurrentUser(String accessToken);

    List<GitHubOwnerResponse> getOrganizations(String accessToken);

    List<GitHubRepoResponse> getAccessibleRepositories(String accessToken);

    List<GitHubWorkflowRunResponse> getWorkflowRuns(String accessToken, String owner, String repo);

    List<GitHubArtifactResponse> getArtifacts(String accessToken, String owner, String repo, Long workflowRunId);

    byte[] downloadArtifact(String accessToken, String owner, String repo, Long artifactId);
}
