package com.sporty.feeds.mapper;

import com.sporty.feeds.exception.InvalidFeedException;
import com.sporty.feeds.model.enums.MessageType;
import com.sporty.feeds.model.enums.OutcomeType;
import com.sporty.feeds.model.StandardBetSettlement;
import com.sporty.feeds.model.StandardMessage;
import com.sporty.feeds.model.StandardOddsChange;
import com.sporty.feeds.model.alpha.AlphaFeedRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProviderAlphaMapperTest {

    private ProviderAlphaMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ProviderAlphaMapper();
    }

    @Test
    void mapsOddsUpdate() {
        var request = new AlphaFeedRequest(
            "odds_update", "ev123",
            Map.of("1", new BigDecimal("2.0"), "X", new BigDecimal("3.1"), "2", new BigDecimal("3.8")),
            null
        );

        StandardMessage result = mapper.map(request);

        assertThat(result).isInstanceOf(StandardOddsChange.class);
        var odds = (StandardOddsChange) result;
        assertThat(odds.eventId()).isEqualTo("ev123");
        assertThat(odds.messageType()).isEqualTo(MessageType.ODDS_CHANGE);
        assertThat(odds.odds().home()).isEqualByComparingTo("2.0");
        assertThat(odds.odds().draw()).isEqualByComparingTo("3.1");
        assertThat(odds.odds().away()).isEqualByComparingTo("3.8");
    }

    @Test
    void mapsSettlementOutcome1ToHome() {
        var request = new AlphaFeedRequest("settlement", "ev123", null, "1");
        StandardMessage result = mapper.map(request);

        assertThat(result).isInstanceOf(StandardBetSettlement.class);
        var settlement = (StandardBetSettlement) result;
        assertThat(settlement.eventId()).isEqualTo("ev123");
        assertThat(settlement.messageType()).isEqualTo(MessageType.BET_SETTLEMENT);
        assertThat(settlement.outcome()).isEqualTo(OutcomeType.HOME);
    }

    @Test
    void mapsSettlementOutcomeXToDraw() {
        var request = new AlphaFeedRequest("settlement", "ev123", null, "X");
        var result = (StandardBetSettlement) mapper.map(request);
        assertThat(result.outcome()).isEqualTo(OutcomeType.DRAW);
    }

    @Test
    void mapsSettlementOutcome2ToAway() {
        var request = new AlphaFeedRequest("settlement", "ev123", null, "2");
        var result = (StandardBetSettlement) mapper.map(request);
        assertThat(result.outcome()).isEqualTo(OutcomeType.AWAY);
    }

    @Test
    void throwsOnUnknownMsgType() {
        var request = new AlphaFeedRequest("unknown", "ev123", null, null);
        assertThatThrownBy(() -> mapper.map(request))
            .isInstanceOf(InvalidFeedException.class)
            .hasMessageContaining("unknown");
    }

    @Test
    void throwsWhenValuesMapIsNull() {
        var request = new AlphaFeedRequest("odds_update", "ev123", null, null);
        assertThatThrownBy(() -> mapper.map(request))
            .isInstanceOf(InvalidFeedException.class)
            .hasMessageContaining("missing required field: values");
    }

    @Test
    void throwsWhenValuesMapHasWrongKeys() {
        var request = new AlphaFeedRequest("odds_update", "ev123",
            Map.of("home", new BigDecimal("2.0"), "draw", new BigDecimal("3.1"), "away", new BigDecimal("3.8")),
            null);
        assertThatThrownBy(() -> mapper.map(request))
            .isInstanceOf(InvalidFeedException.class)
            .hasMessageContaining("must contain keys: 1, X, 2");
    }

    @Test
    void throwsWhenSettlementOutcomeIsNull() {
        var request = new AlphaFeedRequest("settlement", "ev123", null, null);
        assertThatThrownBy(() -> mapper.map(request))
            .isInstanceOf(InvalidFeedException.class)
            .hasMessageContaining("outcome");
    }
}
