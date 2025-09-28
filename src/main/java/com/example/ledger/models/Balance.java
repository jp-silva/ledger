package com.example.ledger.models;

import java.time.OffsetDateTime;

public record Balance(Integer amount, OffsetDateTime updatedAt) {}
