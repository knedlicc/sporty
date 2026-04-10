package com.sporty.feeds.controller;

import com.sporty.feeds.exception.InvalidFeedException;
import com.sporty.feeds.mapper.FeedMapper;
import com.sporty.feeds.model.Odds;
import com.sporty.feeds.model.StandardBetSettlement;
import com.sporty.feeds.model.StandardMessage;
import com.sporty.feeds.model.StandardOddsChange;
import com.sporty.feeds.model.beta.BetaFeedRequest;
import com.sporty.feeds.model.enums.MessageType;
import com.sporty.feeds.model.enums.OutcomeType;
import com.sporty.feeds.model.enums.Provider;
import com.sporty.feeds.queue.MessageQueue;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProviderBetaController.class)
class ProviderBetaControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean FeedMapper<BetaFeedRequest> mapper;
    @MockBean MessageQueue messageQueue;

    @Test
    void acceptsOddsMessageAndDispatchesToQueue() throws Exception {
        var expectedOdds = new StandardOddsChange(
            MessageType.ODDS_CHANGE, Provider.BETA, "ev456",
            new Odds(new BigDecimal("1.95"), new BigDecimal("3.2"), new BigDecimal("4.0")),
            Instant.now()
        );
        when(mapper.map(any())).thenReturn(expectedOdds);

        mockMvc.perform(post("/provider-beta/feed")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "type": "ODDS",
                      "event_id": "ev456",
                      "odds": {"home": 1.95, "draw": 3.2, "away": 4.0}
                    }
                    """))
            .andExpect(status().isOk());

        var captor = ArgumentCaptor.forClass(StandardMessage.class);
        verify(messageQueue).dispatch(captor.capture());
        assertThat(captor.getValue()).isInstanceOf(StandardOddsChange.class);
    }

    @Test
    void acceptsSettlementAndDispatchesToQueue() throws Exception {
        var expectedSettlement = new StandardBetSettlement(
            MessageType.BET_SETTLEMENT, Provider.BETA, "ev456", OutcomeType.AWAY, Instant.now()
        );
        when(mapper.map(any())).thenReturn(expectedSettlement);

        mockMvc.perform(post("/provider-beta/feed")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"type": "SETTLEMENT", "event_id": "ev456", "result": "away"}
                    """))
            .andExpect(status().isOk());

        var captor = ArgumentCaptor.forClass(StandardMessage.class);
        verify(messageQueue).dispatch(captor.capture());
        assertThat(captor.getValue()).isInstanceOf(StandardBetSettlement.class);
    }

    @Test
    void returns400OnUnknownType() throws Exception {
        when(mapper.map(any())).thenThrow(new InvalidFeedException("Unknown Beta type: UNKNOWN", null));

        mockMvc.perform(post("/provider-beta/feed")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"type": "UNKNOWN", "event_id": "ev456"}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Unknown Beta type: UNKNOWN"));
    }

    @Test
    void returns400WhenEventIdIsBlank() throws Exception {
        mockMvc.perform(post("/provider-beta/feed")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"type": "ODDS", "event_id": ""}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("event_id must not be blank"));
    }
}
