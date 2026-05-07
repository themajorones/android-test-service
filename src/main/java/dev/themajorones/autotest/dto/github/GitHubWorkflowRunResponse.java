package dev.themajorones.autotest.dto.github;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GitHubWorkflowRunResponse(
    Long id,
    @JsonProperty("workflow_id") Long workflowId,
    @JsonProperty("head_sha") String headSha,
    String status
) {
}
