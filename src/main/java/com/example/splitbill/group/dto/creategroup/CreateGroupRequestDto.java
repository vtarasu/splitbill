package com.example.splitbill.group.dto.creategroup;

import lombok.Data;

@Data
public class CreateGroupRequestDto {
    private String groupName;
    private long userId;
    private String groupDescription;
}
