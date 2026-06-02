package com.example.splitbill.user.dto;

import lombok.Builder;

import java.math.BigDecimal;


@Builder
public record OwesDto(String from, String to, BigDecimal amount, Direction direction) {
}
