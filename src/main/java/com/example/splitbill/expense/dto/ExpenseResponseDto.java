package com.example.splitbill.expense.dto;

import com.example.splitbill.expense.domain.GroupBalances;
import com.example.splitbill.group.domain.Group;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ExpenseResponseDto {
    private Long id;
    private String description;
    private Long groupId;
    private String groupName;
    private List<GroupBalancesResponseDto> groupBalances;

    public static ExpenseResponseDto from(Long id, Group group, List<GroupBalances> groupBalances) {
        var groupBalancesResponse = groupBalances.stream()
                .map(GroupBalancesResponseDto::from)
                .toList();
        return ExpenseResponseDto.builder()
                .id(id)
                .description("Expense added successfully")
                .groupId(group.getId())
                .groupName(group.getGroupName())
                .groupBalances(groupBalancesResponse)
                .build();
    }
}
