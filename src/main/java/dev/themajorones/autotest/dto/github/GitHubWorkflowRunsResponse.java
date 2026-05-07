package dev.themajorones.autotest.dto.github;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public record GitHubWorkflowRunsResponse(
    @JsonProperty("workflow_runs") List<GitHubWorkflowRunResponse> workflowRuns
) {
}
