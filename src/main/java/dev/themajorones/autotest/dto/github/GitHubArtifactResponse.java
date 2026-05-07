package dev.themajorones.autotest.dto.github;

import java.time.Instant;
import com.fasterxml.jackson.annotation.JsonProperty;

public record GitHubArtifactResponse(
    Long id,
    String name,
    @JsonProperty("size_in_bytes") Long sizeInBytes,
    @JsonProperty("expires_at") Instant expiresAt
) {
}
