package com.sporty.feeds.queue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sporty.feeds.model.StandardMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingMessageQueue implements MessageQueue {

    private static final Logger log = LoggerFactory.getLogger(LoggingMessageQueue.class);
    private final ObjectMapper objectMapper;

    public LoggingMessageQueue(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void dispatch(StandardMessage message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            log.info("[MESSAGE-QUEUE] dispatched: {}", json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to dispatch message", e);
        }
    }
}
