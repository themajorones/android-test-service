package dev.themajorones.autotest.web.rest;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.themajorones.autotest.dto.connection.DockerConnectionRequest;
import dev.themajorones.autotest.dto.connection.OllamaConnectionRequest;
import dev.themajorones.autotest.service.connection.ConnectionManagerService;
import dev.themajorones.models.dto.AndroidVMDetail;
import dev.themajorones.models.dto.CreateAndroidVMRequest;
import dev.themajorones.models.dto.OllamaModelSummary;
import dev.themajorones.models.entity.Docker;
import dev.themajorones.models.entity.Ollama;
import dev.themajorones.models.entity.TaskLog;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ConnectionResource {

    private final ConnectionManagerService service;

    @GetMapping("/api/connections/ollama")
    public List<Ollama> listOllama() {
        return service.listOllama();
    }

    @PostMapping("/api/connections/ollama")
    public Ollama createOllama(@RequestBody OllamaConnectionRequest request) {
        return service.createOllama(request);
    }

    @GetMapping("/api/connections/ollama/{id}")
    public Ollama getOllama(@PathVariable Integer id) {
        return service.getOllama(id);
    }

    @PutMapping("/api/connections/ollama/{id}")
    public Ollama updateOllama(@PathVariable Integer id, @RequestBody OllamaConnectionRequest request) {
        return service.updateOllama(id, request);
    }

    @DeleteMapping("/api/connections/ollama/{id}")
    public ResponseEntity<Void> deleteOllama(@PathVariable Integer id) {
        service.deleteOllama(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/connections/ollama/{id}/models")
    public List<OllamaModelSummary> listOllamaModels(@PathVariable Integer id) {
        return service.listOllamaModels(id);
    }

    @PostMapping("/api/connections/ollama/models")
    public List<OllamaModelSummary> listOllamaModelsForBaseUrl(@RequestBody OllamaConnectionRequest request) {
        return service.listOllamaModels(request.getBaseUrl());
    }

    @GetMapping("/api/connections/docker")
    public List<Docker> listDocker() {
        return service.listDocker();
    }

    @PostMapping("/api/connections/docker")
    public Docker createDocker(@RequestBody DockerConnectionRequest request) {
        return service.createDocker(request);
    }

    @GetMapping("/api/connections/docker/{id}")
    public Docker getDocker(@PathVariable Integer id) {
        return service.getDocker(id);
    }

    @PutMapping("/api/connections/docker/{id}")
    public Docker updateDocker(@PathVariable Integer id, @RequestBody DockerConnectionRequest request) {
        return service.updateDocker(id, request);
    }

    @DeleteMapping("/api/connections/docker/{id}")
    public ResponseEntity<Void> deleteDocker(@PathVariable Integer id) {
        service.deleteDocker(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/connections/android")
    public List<Map<String, Object>> listAndroidVMs() {
        return service.listAndroidVMs();
    }

    @PostMapping("/api/connections/android")
    public ResponseEntity<Map<String, Object>> createAndroidVM(@RequestBody CreateAndroidVMRequest request) {
        return ResponseEntity.accepted().body(service.createAndroidVM(request));
    }

    @GetMapping("/api/connections/android/{id}")
    public AndroidVMDetail getAndroidVM(@PathVariable Integer id) {
        return service.getAndroidVM(id);
    }

    @PostMapping("/api/connections/android/{id}/stop")
    public Map<String, Object> stopAndroidVM(@PathVariable Integer id) {
        return Map.of("androidVM", service.stopAndroidVM(id));
    }

    @DeleteMapping("/api/connections/android/{id}")
    public ResponseEntity<Void> deleteAndroidVM(@PathVariable Integer id) {
        service.deleteAndroidVM(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/task-logs")
    public List<TaskLog> listTaskLogs() {
        return service.listTaskLogs();
    }
}
