package com.sporty.feeds.model;

import com.sporty.feeds.model.enums.MessageType;
import com.sporty.feeds.model.enums.Provider;

import java.time.Instant;

public record StandardOddsChange(
    MessageType messageType,
    Provider provider,
    String eventId,
    Odds odds,
    Instant timestamp
) implements StandardMessage {
    public static StandardOddsChange of(Provider provider, String eventId, Odds odds) {
        return new StandardOddsChange(MessageType.ODDS_CHANGE, provider, eventId, odds, Instant.now());
    }
}
