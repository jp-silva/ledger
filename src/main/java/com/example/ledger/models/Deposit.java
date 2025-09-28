package com.example.ledger.models;

import java.time.OffsetDateTime;
import java.util.UUID;

public record Deposit(UUID id, Integer amount, Integer balance, OffsetDateTime createdAt) {}
