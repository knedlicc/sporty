package com.sporty.feeds.mapper;

import com.sporty.feeds.exception.InvalidFeedException;
import com.sporty.feeds.model.enums.MessageType;
import com.sporty.feeds.model.enums.OutcomeType;
import com.sporty.feeds.model.StandardBetSettlement;
import com.sporty.feeds.model.StandardMessage;
import com.sporty.feeds.model.StandardOddsChange;
import com.sporty.feeds.model.beta.BetaFeedRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProviderBetaMapperTest {

    private ProviderBetaMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ProviderBetaMapper();
    }

    @Test
    void mapsOddsMessage() {
        var request = new BetaFeedRequest(
            "ODDS", "ev456",
            Map.of("home", new BigDecimal("1.95"), "draw", new BigDecimal("3.2"), "away", new BigDecimal("4.0")),
            null
        );

        StandardMessage result = mapper.map(request);

        assertThat(result).isInstanceOf(StandardOddsChange.class);
        var odds = (StandardOddsChange) result;
        assertThat(odds.eventId()).isEqualTo("ev456");
        assertThat(odds.messageType()).isEqualTo(MessageType.ODDS_CHANGE);
        assertThat(odds.odds().home()).isEqualByComparingTo("1.95");
        assertThat(odds.odds().draw()).isEqualByComparingTo("3.2");
        assertThat(odds.odds().away()).isEqualByComparingTo("4.0");
    }

    @Test
    void mapsSettlementResultHomeToHome() {
        var request = new BetaFeedRequest("SETTLEMENT", "ev456", null, "home");
        var result = (StandardBetSettlement) mapper.map(request);
        assertThat(result.outcome()).isEqualTo(OutcomeType.HOME);
        assertThat(result.eventId()).isEqualTo("ev456");
        assertThat(result.messageType()).isEqualTo(MessageType.BET_SETTLEMENT);
    }

    @Test
    void mapsSettlementResultDrawToDraw() {
        var request = new BetaFeedRequest("SETTLEMENT", "ev456", null, "draw");
        var result = (StandardBetSettlement) mapper.map(request);
        assertThat(result.outcome()).isEqualTo(OutcomeType.DRAW);
    }

    @Test
    void mapsSettlementResultAwayToAway() {
        var request = new BetaFeedRequest("SETTLEMENT", "ev456", null, "away");
        var result = (StandardBetSettlement) mapper.map(request);
        assertThat(result.outcome()).isEqualTo(OutcomeType.AWAY);
    }

    @Test
    void throwsOnUnknownType() {
        var request = new BetaFeedRequest("UNKNOWN", "ev456", null, null);
        assertThatThrownBy(() -> mapper.map(request))
            .isInstanceOf(InvalidFeedException.class)
            .hasMessageContaining("UNKNOWN");
    }

    @Test
    void throwsWhenOddsMapIsNull() {
        var request = new BetaFeedRequest("ODDS", "ev456", null, null);
        assertThatThrownBy(() -> mapper.map(request))
            .isInstanceOf(InvalidFeedException.class)
            .hasMessageContaining("missing required field: odds");
    }

    @Test
    void throwsWhenOddsMapHasWrongKeys() {
        var request = new BetaFeedRequest("ODDS", "ev456",
            Map.of("1", new BigDecimal("1.95"), "X", new BigDecimal("3.2"), "2", new BigDecimal("4.0")),
            null);
        assertThatThrownBy(() -> mapper.map(request))
            .isInstanceOf(InvalidFeedException.class)
            .hasMessageContaining("must contain keys: home, draw, away");
    }

    @Test
    void throwsWhenSettlementResultIsNull() {
        var request = new BetaFeedRequest("SETTLEMENT", "ev456", null, null);
        assertThatThrownBy(() -> mapper.map(request))
            .isInstanceOf(InvalidFeedException.class)
            .hasMessageContaining("result");
    }
}
