package dev.themajorones.autotest.service.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import dev.themajorones.autotest.dto.webhook.ReCallWebhookRequest;
import dev.themajorones.autotest.dto.webhook.ResultDTO;
import dev.themajorones.autotest.service.WebhookService;
import org.springframework.stereotype.Service;

@Service
public class WebhookServiceImpl implements WebhookService {

    @Override
    public ResultDTO reCallWebhook(ReCallWebhookRequest request) throws Exception {
        if (request == null) {
            return new ResultDTO("Webhook recall request is required", "INVALID_REQUEST", false);
        }

        Map<String, Object> replayData = new LinkedHashMap<>();
        replayData.put("deliveryId", request.getDeliveryId());
        replayData.put("event", request.getEvent());
        replayData.put("repository", request.getRepository());
        replayData.put("timestamp", request.getTimestamp());
        replayData.put("payload", request.getPayload());

        return new ResultDTO(
                "GitHub webhook recall prepared",
                "OK",
                true,
                replayData,
                1
        );
    }
}
