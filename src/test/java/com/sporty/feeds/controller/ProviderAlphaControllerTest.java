package com.sporty.feeds.controller;

import com.sporty.feeds.exception.InvalidFeedException;
import com.sporty.feeds.mapper.FeedMapper;
import com.sporty.feeds.model.Odds;
import com.sporty.feeds.model.StandardBetSettlement;
import com.sporty.feeds.model.StandardMessage;
import com.sporty.feeds.model.StandardOddsChange;
import com.sporty.feeds.model.alpha.AlphaFeedRequest;
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

@WebMvcTest(ProviderAlphaController.class)
class ProviderAlphaControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean FeedMapper<AlphaFeedRequest> mapper;
    @MockBean MessageQueue messageQueue;

    @Test
    void acceptsOddsUpdateAndDispatchesToQueue() throws Exception {
        var expectedOdds = new StandardOddsChange(
            MessageType.ODDS_CHANGE, Provider.ALPHA, "ev123",
            new Odds(new BigDecimal("2.0"), new BigDecimal("3.1"), new BigDecimal("3.8")),
            Instant.now()
        );
        when(mapper.map(any())).thenReturn(expectedOdds);

        mockMvc.perform(post("/provider-alpha/feed")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "msg_type": "odds_update",
                      "event_id": "ev123",
                      "values": {"1": 2.0, "X": 3.1, "2": 3.8}
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
            MessageType.BET_SETTLEMENT, Provider.ALPHA, "ev123", OutcomeType.HOME, Instant.now()
        );
        when(mapper.map(any())).thenReturn(expectedSettlement);

        mockMvc.perform(post("/provider-alpha/feed")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"msg_type": "settlement", "event_id": "ev123", "outcome": "1"}
                    """))
            .andExpect(status().isOk());

        var captor = ArgumentCaptor.forClass(StandardMessage.class);
        verify(messageQueue).dispatch(captor.capture());
        assertThat(captor.getValue()).isInstanceOf(StandardBetSettlement.class);
    }

    @Test
    void returns400OnUnknownMsgType() throws Exception {
        when(mapper.map(any())).thenThrow(new InvalidFeedException("Unknown Alpha msg_type: unknown", null));

        mockMvc.perform(post("/provider-alpha/feed")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"msg_type": "unknown", "event_id": "ev123"}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Unknown Alpha msg_type: unknown"));
    }

    @Test
    void returns400WhenEventIdIsBlank() throws Exception {
        mockMvc.perform(post("/provider-alpha/feed")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"msg_type": "odds_update", "event_id": "", "values": {"1": 2.0, "X": 3.1, "2": 3.8}}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("event_id must not be blank"));
    }
}
