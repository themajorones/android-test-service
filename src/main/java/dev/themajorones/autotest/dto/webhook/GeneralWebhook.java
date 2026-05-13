package dev.themajorones.autotest.dto.webhook;

import tools.jackson.databind.JsonNode;

public class GeneralWebhook {
    protected int code;
    protected JsonNode rawData;
    protected Long timestamp;

    public GeneralWebhook(){}

    public GeneralWebhook(int code, JsonNode rawData, Long timestamp) {
        this.code = code;
        this.rawData = rawData;
        this.timestamp = timestamp;
    }

    public GeneralWebhook(GeneralWebhook other) {
        this.code = other.code;
        this.rawData = other.rawData;
        this.timestamp = other.timestamp;
    }

    public int getCode() { return code; }

    public void setCode(int code) { this.code = code; }

    public JsonNode getRawData() { return rawData; }

    public void setRawData(JsonNode rawData) { this.rawData = rawData; }

    public Long getTimestamp() { return timestamp; }

    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
}
