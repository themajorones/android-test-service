package dev.themajorones.autotest.dto.github;

import java.util.List;

public record GitHubArtifactsResponse(List<GitHubArtifactResponse> artifacts) {
}
