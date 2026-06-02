package com.example.splitbill.user.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class GetGroupAndBalances {
    private String groupName;
    private Long groupId;
    private Integer memberCount;
    private List<OwesDto> balances;
    private List<UserRecord> members;
}
