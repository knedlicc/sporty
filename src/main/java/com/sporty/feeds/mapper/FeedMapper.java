package com.sporty.feeds.mapper;

import com.sporty.feeds.model.StandardMessage;

public interface FeedMapper<T> {
    StandardMessage map(T request);
}
