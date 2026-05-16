package com.example.splitbill.expense.dto;

import com.example.splitbill.expense.domain.GroupBalances;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class GroupBalancesResponseDto {
    Long id;
    Long fromId;
    String fromUserName;
    Long toId;
    String toUserName;
    BigDecimal amount;

    public static GroupBalancesResponseDto from(GroupBalances groupBalances) {
        return GroupBalancesResponseDto.builder()
                .id(groupBalances.getId())
                .fromId(groupBalances.getFrom().getId())
                .toId(groupBalances.getTo().getId())
                .fromUserName(groupBalances.getFrom().getUsername())
                .toUserName(groupBalances.getTo().getUsername())
                .amount(groupBalances.getBalance())
                .build();
    }
}
