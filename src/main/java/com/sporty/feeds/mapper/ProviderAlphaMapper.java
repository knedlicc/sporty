package com.sporty.feeds.mapper;

import com.sporty.feeds.exception.InvalidFeedException;
import com.sporty.feeds.model.Odds;
import com.sporty.feeds.model.StandardBetSettlement;
import com.sporty.feeds.model.StandardMessage;
import com.sporty.feeds.model.StandardOddsChange;
import com.sporty.feeds.model.alpha.AlphaFeedRequest;
import com.sporty.feeds.model.enums.OutcomeType;
import com.sporty.feeds.model.enums.Provider;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class ProviderAlphaMapper implements FeedMapper<AlphaFeedRequest> {

    @Override
    public StandardMessage map(AlphaFeedRequest request) {
        return switch (request.msgType()) {
            case "odds_update" -> mapOddsChange(request);
            case "settlement"  -> mapSettlement(request);
            default -> throw new InvalidFeedException("Unknown Alpha msg_type: " + request.msgType(), request);
        };
    }

    private StandardOddsChange mapOddsChange(AlphaFeedRequest request) {
        Map<String, BigDecimal> values = request.values();
        if (values == null) {
            throw new InvalidFeedException("Alpha odds_update missing required field: values", request);
        }
        if (!values.containsKey("1") || !values.containsKey("X") || !values.containsKey("2")) {
            throw new InvalidFeedException("Alpha odds_update values must contain keys: 1, X, 2 (got: " + values.keySet() + ")", request);
        }
        return StandardOddsChange.of(
            Provider.ALPHA,
            request.eventId(),
            new Odds(values.get("1"), values.get("X"), values.get("2"))
        );
    }

    private StandardBetSettlement mapSettlement(AlphaFeedRequest request) {
        if (request.outcome() == null) {
            throw new InvalidFeedException("Alpha settlement missing required field: outcome", request);
        }
        OutcomeType outcome = switch (request.outcome()) {
            case "1" -> OutcomeType.HOME;
            case "X" -> OutcomeType.DRAW;
            case "2" -> OutcomeType.AWAY;
            default  -> throw new InvalidFeedException("Unknown Alpha outcome: " + request.outcome(), request);
        };
        return StandardBetSettlement.of(Provider.ALPHA, request.eventId(), outcome);
    }
}
