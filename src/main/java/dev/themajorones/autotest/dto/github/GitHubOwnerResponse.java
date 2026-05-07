package dev.themajorones.autotest.dto.github;

public record GitHubOwnerResponse(
    Long id,
    String login,
    String name,
    String type
) {
}
