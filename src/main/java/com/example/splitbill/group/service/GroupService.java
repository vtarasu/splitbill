package com.example.splitbill.group.service;

import com.example.splitbill.expense.repo.GroupBalancesRepo;
import com.example.splitbill.group.dto.creategroup.CreateGroupRequestDto;
import com.example.splitbill.group.dto.creategroup.CreateGroupResponseDto;
import com.example.splitbill.group.dto.creategroup.UpdateGroupRequestDto;
import com.example.splitbill.group.dto.removeuser.RemoveUserFromGroupDto;
import com.example.splitbill.group.exception.CannotRemoveUserException;
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
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Objects;

@Slf4j
@Service
public class GroupService {
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final UserGroupRepository userGroupRepository;
    private final GroupBalancesRepo groupBalancesRepo;

    public GroupService(GroupRepository groupRepository, UserRepository userRepository,
                        UserGroupRepository userGroupRepository, GroupBalancesRepo groupBalancesRepo) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.userGroupRepository = userGroupRepository;
        this.groupBalancesRepo = groupBalancesRepo;
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
                .orElseThrow(() -> new GroupDoesNotExistsException("Invalid Group"));

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

    @Transactional
    public void removeUserFromGroup(RemoveUserFromGroupDto removeUserFromGroupDto) {
        var userGroup = userGroupRepository.findByUserIdAndGroupId(removeUserFromGroupDto.getUserId(),
                        removeUserFromGroupDto.getGroupId())
                .orElseThrow(() -> new UserDoesNotExistsException("User/Group doesn't exist"));

        var balances = groupBalancesRepo.findByGroupIdAndFromIdOrGroupIdAndToId(
                removeUserFromGroupDto.getGroupId(), removeUserFromGroupDto.getUserId(),
                removeUserFromGroupDto.getGroupId(), removeUserFromGroupDto.getUserId());

        if (!balances.isEmpty()) {
            throw new CannotRemoveUserException("User with unresolved balances cannot be removed from group");
        }

        userGroup.getUser().getUserGroups().remove(removeUserFromGroupDto.getGroupId());
        userGroup.getGroup().getUsers().remove(removeUserFromGroupDto.getUserId());
        userGroupRepository.delete(userGroup);
    }

    @Transactional
    public UpdateGroupRequestDto updateGroup(UpdateGroupRequestDto updateGroupRequestDto) {
        var group = groupRepository.findGroupById(updateGroupRequestDto.getGroupId())
                .orElseThrow(() -> new GroupDoesNotExistsException("Group doesn't exists"));

        if (Objects.nonNull(updateGroupRequestDto.getGroupDescription())) {
            group.setGroupDescription(updateGroupRequestDto.getGroupDescription());
        }
        return updateGroupRequestDto;
    }
}
