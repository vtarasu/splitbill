package com.example.splitbill.group.dto.creategroup;

import lombok.Data;

import java.util.List;

@Data
public class CreateGroupRequestDto {
    private String groupName;
    private List<String> groupMembers;
}
