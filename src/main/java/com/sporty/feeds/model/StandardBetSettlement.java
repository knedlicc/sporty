package com.sporty.feeds.model;

import com.sporty.feeds.model.enums.MessageType;
import com.sporty.feeds.model.enums.OutcomeType;
import com.sporty.feeds.model.enums.Provider;

import java.time.Instant;

public record StandardBetSettlement(
    MessageType messageType,
    Provider provider,
    String eventId,
    OutcomeType outcome,
    Instant timestamp
) implements StandardMessage {
    public static StandardBetSettlement of(Provider provider, String eventId, OutcomeType outcome) {
        return new StandardBetSettlement(MessageType.BET_SETTLEMENT, provider, eventId, outcome, Instant.now());
    }
}
