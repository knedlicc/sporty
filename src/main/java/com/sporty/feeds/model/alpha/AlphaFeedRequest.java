package com.sporty.feeds.model.alpha;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.util.Map;

public record AlphaFeedRequest(
    @NotBlank(message = "msg_type must not be blank") @JsonProperty("msg_type") String msgType,
    @NotBlank(message = "event_id must not be blank") @JsonProperty("event_id") String eventId,
    Map<String, BigDecimal> values,
    String outcome
) {}
