package com.example.splitbill.group.controller;

import com.example.splitbill.group.dto.adduser.AddUserToGroupDto;
import com.example.splitbill.group.dto.creategroup.CreateGroupRequestDto;
import com.example.splitbill.group.dto.creategroup.CreateGroupResponseDto;
import com.example.splitbill.group.service.GroupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/group")
public class GroupController {
    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping("/create")
    public CreateGroupResponseDto createGroup(@RequestBody CreateGroupRequestDto createGroupDto) {
        return groupService.createGroup(createGroupDto);
    }

    @PostMapping("/add-user")
    public AddUserToGroupDto addUserToGroup(@RequestBody AddUserToGroupDto addUserToGroupDto) {
        return groupService.addUserToGroup(addUserToGroupDto);
    }
}
