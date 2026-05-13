package dev.themajorones.autotest.dto.webhook;

import tools.jackson.databind.JsonNode;

public class ReCallWebhookRequest {
	private String deliveryId;
	private String event;
	private String repository;
	private JsonNode payload;
	private Long timestamp;

	public ReCallWebhookRequest() {}

	public ReCallWebhookRequest(String deliveryId, String event, String repository, JsonNode payload, Long timestamp) {
		this.deliveryId = deliveryId; //X-GitHub-Delivery
		this.event = event; //X-GitHub-Event
		this.repository = repository;
		this.payload = payload;
		this.timestamp = timestamp;
	}

	public String getDeliveryId() {
		return deliveryId;
	}

	public void setDeliveryId(String deliveryId) {
		this.deliveryId = deliveryId;
	}

	public String getEvent() {
		return event;
	}

	public void setEvent(String event) {
		this.event = event;
	}

	public String getRepository() {
		return repository;
	}

	public void setRepository(String repository) {
		this.repository = repository;
	}

	public JsonNode getPayload() {
		return payload;
	}

	public void setPayload(JsonNode payload) {
		this.payload = payload;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}
}
