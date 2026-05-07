package dev.themajorones.autotest.dto.github;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GitHubRepoResponse(
    Long id,
    GitHubOwnerResponse owner,
    String name,
    @JsonProperty("full_name") String fullName,
    @JsonProperty("private") boolean privateRepo
) {
}
