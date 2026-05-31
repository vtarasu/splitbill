package com.example.splitbill.group.controller;

import com.example.splitbill.group.dto.adduser.AddUserToGroupDto;
import com.example.splitbill.group.dto.creategroup.CreateGroupRequestDto;
import com.example.splitbill.group.dto.creategroup.UpdateGroupRequestDto;
import com.example.splitbill.group.dto.removeuser.RemoveUserFromGroupDto;
import com.example.splitbill.group.exception.CannotRemoveGroupException;
import com.example.splitbill.group.exception.CannotRemoveUserException;
import com.example.splitbill.group.service.GroupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/group")
public class GroupController {
    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createGroup(@RequestBody CreateGroupRequestDto createGroupDto) {
        log.info("Received request to create group. createGroup={}", createGroupDto);
        try {
            var response = groupService.createGroup(createGroupDto);
            log.info("Group created successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Cannot error group.", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Group creation failed. "+e.getLocalizedMessage());
        }
    }

    @PostMapping("/update")
    public UpdateGroupRequestDto updateGroup(@RequestBody UpdateGroupRequestDto updateGroupRequestDto) {
        return groupService.updateGroup(updateGroupRequestDto);
    }

    @PostMapping("/add-user")
    public AddUserToGroupDto addUserToGroup(@RequestBody AddUserToGroupDto addUserToGroupDto) {
        return groupService.addUserToGroup(addUserToGroupDto);
    }

    @PostMapping("/remove-user")
    public ResponseEntity<String> removeUserFromGroup(@RequestBody RemoveUserFromGroupDto removeUserFromGroupDto) {
        try {
            groupService.removeUserFromGroup(removeUserFromGroupDto);
            return ResponseEntity.ok("Removed user from group");
        } catch (CannotRemoveUserException | Exception e) {
            log.error("Cannot remove user from group", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Cannot remove user. message=" + e.getLocalizedMessage());
        }

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGroup(@PathVariable Long id) {
        try {
            groupService.deleteGroup(id);
            return ResponseEntity.ok("Group deleted successfully");
        } catch (Exception | CannotRemoveGroupException e) {
            log.error("Failed to delete group={}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete group. message=" + e.getLocalizedMessage());
        }
    }
}
