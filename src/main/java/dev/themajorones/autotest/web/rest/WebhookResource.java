package dev.themajorones.autotest.web.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WebhookResource {
    
    @PostMapping("/webhook/github")
    public ResponseEntity<Void> handleWebhook(@RequestBody Object payload) {
        System.out.println("Received webhook: " + payload);
        return ResponseEntity.ok().build();
    }
}