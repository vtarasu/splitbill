package com.example.splitbill.group.dto.removeuser;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class RemoveUserFromGroupDto {
    private Long userId;
    private Long groupId;
}
