package com.example.splitbill.user.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Builder
@Data
public class GetUserGroupsAndBalancesDto {
    private String groupName;
    private Long groupId;
    private Map<Long, Double> balances;
}
