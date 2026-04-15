package com.example.splitbill.group;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateGroupResponseDto {
    private long groupId;
    private String groupName;

    public static CreateGroupResponseDto from(Group savedGroup) {
        return CreateGroupResponseDto.builder()
                .groupId(savedGroup.getId())
                .groupName(savedGroup.getGroupName())
                .build();
    }
}
