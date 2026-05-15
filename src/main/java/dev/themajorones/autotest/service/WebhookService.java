package dev.themajorones.autotest.service;
import dev.themajorones.autotest.dto.webhook.ReCallWebhookRequest;
import dev.themajorones.autotest.dto.webhook.ResultDTO;
import tools.jackson.databind.JsonNode;

public interface WebhookService {

    ResultDTO reCallWebhook(ReCallWebhookRequest request) throws Exception;

    void passWebhookToWebservice(JsonNode payload, String serviceUrl);
}
