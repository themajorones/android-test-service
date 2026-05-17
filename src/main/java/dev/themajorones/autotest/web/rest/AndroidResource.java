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
import org.springframework.web.bind.annotation.RestController;

import dev.themajorones.autotest.service.connection.ConnectionManagerService;
import dev.themajorones.models.dto.AndroidVMDetail;
import dev.themajorones.models.dto.CreateAndroidVMRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AndroidResource {

    private final ConnectionManagerService service;

    @GetMapping("/api/connections/android")
    public List<Map<String, Object>> listAndroidVMs() {
        return service.listAndroidVMs();
    }

    @PostMapping("/api/connections/android")
    public ResponseEntity<Map<String, Object>> createAndroidVM(@RequestBody CreateAndroidVMRequest request) {
        return ResponseEntity.accepted().body(service.createAndroidVM(request));
    }

    @PutMapping("/api/connections/android/{id}")
    public Map<String, Object> updateAndroidVM(@PathVariable Integer id, @RequestBody CreateAndroidVMRequest request) {
        return service.updateAndroidVM(id, request);
    }

    @GetMapping("/api/connections/android/{id}")
    public AndroidVMDetail getAndroidVM(@PathVariable Integer id) {
        return service.getAndroidVM(id);
    }

    @PostMapping("/api/connections/android/{id}/stop")
    public Map<String, Object> stopAndroidVM(@PathVariable Integer id) {
        return service.stopAndroidVM(id);
    }

    @PostMapping("/api/connections/android/health")
    public Map<String, Object> refreshAndroidHealth() {
        return Map.of("checked", service.refreshAndroidHealth());
    }

    @PostMapping("/api/connections/android/{id}/health")
    public Map<String, Object> refreshAndroidHealth(@PathVariable Integer id) {
        return service.refreshAndroidHealth(id);
    }

    @DeleteMapping("/api/connections/android/{id}")
    public ResponseEntity<Void> deleteAndroidVM(@PathVariable Integer id) {
        service.deleteAndroidVM(id);
        return ResponseEntity.noContent().build();
    }
}

@RestController
@RequiredArgsConstructor
class ConnectionsResource {

    private final ConnectionManagerService service;

    @PostMapping("/api/connections/health")
    public Map<String, Object> refreshAllHealth() {
        return service.refreshConnectionHealth();
    }
}
