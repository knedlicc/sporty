package com.sporty.feeds.model;

public sealed interface StandardMessage permits StandardOddsChange, StandardBetSettlement {}
