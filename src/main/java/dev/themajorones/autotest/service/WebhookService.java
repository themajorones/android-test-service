package dev.themajorones.autotest.service;

import dev.themajorones.autotest.dto.webhook.ReCallWebhookRequest;
import dev.themajorones.autotest.dto.webhook.ResultDTO;

public interface WebhookService {

        ResultDTO reCallWebhook(ReCallWebhookRequest request) throws Exception;
}
