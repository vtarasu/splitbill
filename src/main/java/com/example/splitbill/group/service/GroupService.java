package com.example.splitbill.group.service;

import com.example.splitbill.group.dto.creategroup.CreateGroupRequestDto;
import com.example.splitbill.group.dto.creategroup.CreateGroupResponseDto;
import com.example.splitbill.group.repo.UserGroupRepository;
import com.example.splitbill.group.dto.adduser.AddUserToGroupDto;
import com.example.splitbill.group.domain.Group;
import com.example.splitbill.group.domain.UserGroup;
import com.example.splitbill.group.exception.GroupAlreadyExistsException;
import com.example.splitbill.group.exception.GroupDoesNotExistsException;
import com.example.splitbill.group.repo.GroupRepository;
import com.example.splitbill.user.exception.UserDoesNotExistsException;
import com.example.splitbill.user.repo.UserRepository;
import com.example.splitbill.user.dto.UserRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Slf4j
@Service
public class GroupService {
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final UserGroupRepository userGroupRepository;

    public GroupService(GroupRepository groupRepository, UserRepository userRepository,
                        UserGroupRepository userGroupRepository) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.userGroupRepository = userGroupRepository;
    }

    public CreateGroupResponseDto createGroup(CreateGroupRequestDto createGroupRequestDto) {
        var user = userRepository.findUserById(createGroupRequestDto.getUserId())
                .orElseThrow(() -> new UserDoesNotExistsException("User not exists."));

        var existingGroup = groupRepository.findGroupByGroupName(createGroupRequestDto.getGroupName());
        if (existingGroup.isPresent()) {
            throw new GroupAlreadyExistsException(String.format("Group with name : %s  already exists.",
                    createGroupRequestDto.getGroupName()));
        }

        var group = Group.builder()
                .groupName(createGroupRequestDto.getGroupName())
                .groupDescription(createGroupRequestDto.getGroupDescription())
                .users(new ArrayList<>())
                .build();

        var userGroup = UserGroup.builder()
                .user(user)
                .group(group)
                .userrole(UserRole.ADMIN)
                .build();
        group.getUsers().add(userGroup);
        user.getUserGroups().add(userGroup);
        var savedGroup = groupRepository.save(group);
        return CreateGroupResponseDto.from(savedGroup);
    }

    public AddUserToGroupDto addUserToGroup(AddUserToGroupDto addUserToGroupDto) {
        var group = groupRepository.findGroupById(addUserToGroupDto.getGroupId())
                .orElseThrow(() ->new GroupDoesNotExistsException("Invalid Group"));

        var users = addUserToGroupDto.getUserId()
                .stream().map(id -> userRepository.findUserById(id)
                        .orElseThrow(() -> new UserDoesNotExistsException("User not exists")))
                .toList();

        var userGroups = users.stream()
                .map(user -> UserGroup.builder().user(user)
                        .group(group)
                        .userrole(UserRole.MEMBER)
                        .build())
                .toList();
        var savedUserGroups = userGroupRepository.saveAll(userGroups);
        var savedUsers = savedUserGroups.stream().map(userGroup -> userGroup.getUser().getId()).toList();
        var savedGroup = savedUserGroups.stream().map(userGroup -> userGroup.getGroup().getId()).toList();
        return AddUserToGroupDto.builder()
                .userId(savedUsers)
                .groupId(savedGroup.getFirst())
                .build();
    }
}
