package com.sporty.feeds.mapper;

import com.sporty.feeds.exception.InvalidFeedException;
import com.sporty.feeds.model.Odds;
import com.sporty.feeds.model.StandardBetSettlement;
import com.sporty.feeds.model.StandardMessage;
import com.sporty.feeds.model.StandardOddsChange;
import com.sporty.feeds.model.beta.BetaFeedRequest;
import com.sporty.feeds.model.enums.OutcomeType;
import com.sporty.feeds.model.enums.Provider;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class ProviderBetaMapper implements FeedMapper<BetaFeedRequest> {

    @Override
    public StandardMessage map(BetaFeedRequest request) {
        return switch (request.type()) {
            case "ODDS"       -> mapOddsChange(request);
            case "SETTLEMENT" -> mapSettlement(request);
            default -> throw new InvalidFeedException("Unknown Beta type: " + request.type(), request);
        };
    }

    private StandardOddsChange mapOddsChange(BetaFeedRequest request) {
        Map<String, BigDecimal> odds = request.odds();
        if (odds == null) {
            throw new InvalidFeedException("Beta ODDS missing required field: odds", request);
        }
        if (!odds.containsKey("home") || !odds.containsKey("draw") || !odds.containsKey("away")) {
            throw new InvalidFeedException("Beta ODDS values must contain keys: home, draw, away (got: " + odds.keySet() + ")", request);
        }
        return StandardOddsChange.of(
            Provider.BETA,
            request.eventId(),
            new Odds(odds.get("home"), odds.get("draw"), odds.get("away"))
        );
    }

    private StandardBetSettlement mapSettlement(BetaFeedRequest request) {
        if (request.result() == null) {
            throw new InvalidFeedException("Beta SETTLEMENT missing required field: result", request);
        }
        OutcomeType outcome = switch (request.result()) {
            case "home" -> OutcomeType.HOME;
            case "draw" -> OutcomeType.DRAW;
            case "away" -> OutcomeType.AWAY;
            default     -> throw new InvalidFeedException("Unknown Beta result: " + request.result(), request);
        };
        return StandardBetSettlement.of(Provider.BETA, request.eventId(), outcome);
    }
}
