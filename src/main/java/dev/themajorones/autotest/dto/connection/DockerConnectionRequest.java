package dev.themajorones.autotest.dto.connection;

import lombok.Data;

@Data
public class DockerConnectionRequest {

    private String name;
    private String baseUrl;
    private Boolean enabled;
}
