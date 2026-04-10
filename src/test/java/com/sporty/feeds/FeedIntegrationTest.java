package com.sporty.feeds;

import com.sporty.feeds.model.StandardBetSettlement;
import com.sporty.feeds.model.StandardMessage;
import com.sporty.feeds.model.StandardOddsChange;
import com.sporty.feeds.model.enums.MessageType;
import com.sporty.feeds.model.enums.OutcomeType;
import com.sporty.feeds.model.enums.Provider;
import com.sporty.feeds.queue.LoggingMessageQueue;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FeedIntegrationTest {

    @Autowired MockMvc mockMvc;
    @SpyBean LoggingMessageQueue messageQueue;

    @Test
    void alphaOddsUpdateIsNormalizedAndDispatched() throws Exception {
        mockMvc.perform(post("/provider-alpha/feed")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"msg_type":"odds_update","event_id":"ev123","values":{"1":2.0,"X":3.1,"2":3.8}}
                    """))
            .andExpect(status().isOk());

        var captor = ArgumentCaptor.forClass(StandardMessage.class);
        verify(messageQueue).dispatch(captor.capture());
        var message = (StandardOddsChange) captor.getValue();
        assertThat(message.messageType()).isEqualTo(MessageType.ODDS_CHANGE);
        assertThat(message.provider()).isEqualTo(Provider.ALPHA);
        assertThat(message.eventId()).isEqualTo("ev123");
        assertThat(message.odds().home()).isEqualByComparingTo("2.0");
        assertThat(message.odds().draw()).isEqualByComparingTo("3.1");
        assertThat(message.odds().away()).isEqualByComparingTo("3.8");
    }

    @Test
    void betaSettlementIsNormalizedAndDispatched() throws Exception {
        mockMvc.perform(post("/provider-beta/feed")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"type":"SETTLEMENT","event_id":"ev456","result":"away"}
                    """))
            .andExpect(status().isOk());

        var captor = ArgumentCaptor.forClass(StandardMessage.class);
        verify(messageQueue).dispatch(captor.capture());
        var message = (StandardBetSettlement) captor.getValue();
        assertThat(message.messageType()).isEqualTo(MessageType.BET_SETTLEMENT);
        assertThat(message.provider()).isEqualTo(Provider.BETA);
        assertThat(message.eventId()).isEqualTo("ev456");
        assertThat(message.outcome()).isEqualTo(OutcomeType.AWAY);
    }
}
