package com.example.splitbill.user.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Builder
@Data
public class GetUserGroupsAndBalancesDto {
    private String groupName;
    private Long groupId;
    private Integer memberCount;
    private List<OwesDto> balances;
}
