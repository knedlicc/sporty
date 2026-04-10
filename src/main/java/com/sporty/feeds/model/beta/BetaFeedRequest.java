package com.sporty.feeds.model.beta;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.util.Map;

public record BetaFeedRequest(
    @NotBlank(message = "type must not be blank") String type,
    @NotBlank(message = "event_id must not be blank") @JsonProperty("event_id") String eventId,
    Map<String, BigDecimal> odds,
    String result
) {}
