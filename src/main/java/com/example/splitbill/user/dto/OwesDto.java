package com.example.splitbill.user.dto;

import java.math.BigDecimal;

public record OwesDto(String from, String to, BigDecimal amount) {
}
