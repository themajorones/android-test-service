package dev.themajorones.autotest.web.rest;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.themajorones.autotest.service.connection.ConnectionManagerService;
import dev.themajorones.models.entity.TaskLog;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class TaskLogResource {

    private final ConnectionManagerService service;

    @GetMapping("/api/task-logs")
    public List<TaskLog> listTaskLogs() {
        return service.listTaskLogs();
    }
}
