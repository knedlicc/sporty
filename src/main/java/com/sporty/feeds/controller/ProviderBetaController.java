package com.sporty.feeds.controller;

import com.sporty.feeds.mapper.FeedMapper;
import com.sporty.feeds.model.StandardMessage;
import com.sporty.feeds.model.beta.BetaFeedRequest;
import com.sporty.feeds.queue.MessageQueue;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/provider-beta")
public class ProviderBetaController {

    private final FeedMapper<BetaFeedRequest> mapper;
    private final MessageQueue messageQueue;

    public ProviderBetaController(FeedMapper<BetaFeedRequest> mapper, MessageQueue messageQueue) {
        this.mapper = mapper;
        this.messageQueue = messageQueue;
    }

    @PostMapping("/feed")
    public ResponseEntity<Void> handleFeed(@Valid @RequestBody BetaFeedRequest request) {
        StandardMessage standardMessage = mapper.map(request);
        messageQueue.dispatch(standardMessage);
        return ResponseEntity.ok().build();
    }

}
