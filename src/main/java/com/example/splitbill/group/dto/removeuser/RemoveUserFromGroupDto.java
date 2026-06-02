package com.example.splitbill.group.dto.removeuser;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class RemoveUserFromGroupDto {
    private List<Long> userId;
    private Long groupId;
}
