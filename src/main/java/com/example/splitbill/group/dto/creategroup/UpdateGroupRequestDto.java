package com.example.splitbill.group.dto.creategroup;

import lombok.Data;

@Data
public class UpdateGroupRequestDto {
    private long groupId;
    private String groupDescription;
}
