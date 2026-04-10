package com.sporty.feeds.model;

import java.math.BigDecimal;

public record Odds(BigDecimal home, BigDecimal draw, BigDecimal away) {}
