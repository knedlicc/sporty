package com.sporty.feeds.queue;

import com.sporty.feeds.model.StandardMessage;

public interface MessageQueue {
    void dispatch(StandardMessage message);
}
