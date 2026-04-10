package com.sporty.feeds.controller;

import com.sporty.feeds.mapper.FeedMapper;
import com.sporty.feeds.model.StandardMessage;
import com.sporty.feeds.model.alpha.AlphaFeedRequest;
import com.sporty.feeds.queue.MessageQueue;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/provider-alpha")
public class ProviderAlphaController {

    private final FeedMapper<AlphaFeedRequest> mapper;
    private final MessageQueue messageQueue;

    public ProviderAlphaController(FeedMapper<AlphaFeedRequest> mapper, MessageQueue messageQueue) {
        this.mapper = mapper;
        this.messageQueue = messageQueue;
    }

    @PostMapping("/feed")
    public ResponseEntity<Void> handleFeed(@Valid @RequestBody AlphaFeedRequest request) {
        StandardMessage standardMessage = mapper.map(request);
        messageQueue.dispatch(standardMessage);
        return ResponseEntity.ok().build();
    }

}
