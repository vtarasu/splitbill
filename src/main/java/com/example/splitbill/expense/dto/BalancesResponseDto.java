package com.example.splitbill.expense.dto;

import com.example.splitbill.expense.domain.GroupBalances;
import com.example.splitbill.expense.domain.NonGroupBalance;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class BalancesResponseDto {
    Long id;
    Long fromId;
    String fromUserName;
    Long toId;
    String toUserName;
    BigDecimal amount;

    public static BalancesResponseDto from(GroupBalances groupBalances) {
        return BalancesResponseDto.builder()
                .id(groupBalances.getId())
                .fromId(groupBalances.getFrom().getId())
                .toId(groupBalances.getTo().getId())
                .fromUserName(groupBalances.getFrom().getUsername())
                .toUserName(groupBalances.getTo().getUsername())
                .amount(groupBalances.getBalance())
                .build();
    }

    public static BalancesResponseDto from(NonGroupBalance balances) {
        return BalancesResponseDto.builder()
                .id(balances.getId())
                .fromId(balances.getFrom().getId())
                .toId(balances.getTo().getId())
                .fromUserName(balances.getFrom().getUsername())
                .toUserName(balances.getTo().getUsername())
                .amount(balances.getBalance())
                .build();
    }
}
