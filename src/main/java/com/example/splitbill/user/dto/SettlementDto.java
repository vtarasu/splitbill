package com.example.splitbill.user.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record SettlementDto(Long id, String from, String to, BigDecimal amount, LocalDateTime settledAt) {
}
