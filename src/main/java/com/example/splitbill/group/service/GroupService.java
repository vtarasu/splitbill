package com.example.splitbill.group.service;

import com.example.splitbill.expense.repo.GroupBalancesRepo;
import com.example.splitbill.group.dto.creategroup.CreateGroupRequestDto;
import com.example.splitbill.group.dto.creategroup.UpdateGroupRequestDto;
import com.example.splitbill.group.dto.removeuser.RemoveUserFromGroupDto;
import com.example.splitbill.group.exception.CannotRemoveGroupException;
import com.example.splitbill.group.exception.CannotRemoveUserException;
import com.example.splitbill.group.repo.UserGroupRepository;
import com.example.splitbill.group.dto.adduser.AddUserToGroupDto;
import com.example.splitbill.group.domain.Group;
import com.example.splitbill.group.domain.UserGroup;
import com.example.splitbill.group.exception.GroupAlreadyExistsException;
import com.example.splitbill.group.exception.GroupDoesNotExistsException;
import com.example.splitbill.group.repo.GroupRepository;
import com.example.splitbill.user.dto.GetGroupAndBalances;
import com.example.splitbill.user.dto.OwesDto;
import com.example.splitbill.user.dto.UserRecord;
import com.example.splitbill.user.exception.UserAlreadyExistsException;
import com.example.splitbill.user.exception.UserDoesNotExistsException;
import com.example.splitbill.user.repo.UserRepository;
import com.example.splitbill.user.service.UserBalancesService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.splitbill.user.dto.UserRole.ADMIN;
import static com.example.splitbill.user.dto.UserRole.MEMBER;

@Slf4j
@Service
public class GroupService {
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final UserGroupRepository userGroupRepository;
    private final GroupBalancesRepo groupBalancesRepo;
    private final UserBalancesService userBalancesService;

    public GroupService(GroupRepository groupRepository, UserRepository userRepository,
                        UserGroupRepository userGroupRepository, GroupBalancesRepo groupBalancesRepo, UserBalancesService userBalancesService) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.userGroupRepository = userGroupRepository;
        this.groupBalancesRepo = groupBalancesRepo;
        this.userBalancesService = userBalancesService;
    }

    public GetGroupAndBalances createGroup(CreateGroupRequestDto createGroupRequestDto) {
        var userId = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        var user = userRepository.findUserById((Long) userId)
                .orElseThrow(() -> new UserDoesNotExistsException("User not exists."));

        var existingGroup = groupRepository.findGroupByGroupName(createGroupRequestDto.getGroupName());
        if (existingGroup.isPresent()) {
            throw new GroupAlreadyExistsException(String.format("Group with name : %s  already exists.",
                    createGroupRequestDto.getGroupName()));
        }

        var group = Group.builder()
                .groupName(createGroupRequestDto.getGroupName())
                .createdBy(user)
                .users(new ArrayList<>())
                .build();

        createGroupRequestDto.getGroupMembers().add(user.getUsername());
        var groupUsers = createGroupRequestDto.getGroupMembers()
                .stream().map(username -> userRepository.findUserByUsername(username)
                        .orElseThrow(() -> new UserDoesNotExistsException("User "+ username + " not exists")))
                .toList();

        var userGroups = groupUsers.stream()
                .map(groupUser -> UserGroup.builder().user(groupUser)
                        .group(group)
                        .userrole(groupUser.getUsername().equals(user.getUsername()) ? ADMIN : MEMBER)
                        .build())
                .toList();

        group.getUsers().addAll(userGroups);
        user.getUserGroups().addAll(userGroups);
        var savedGroup = groupRepository.save(group);
        return GetGroupAndBalances.builder()
                .groupId(savedGroup.getId())
                .groupName(savedGroup.getGroupName())
                .memberCount(groupUsers.size())
                .balances(new ArrayList<>())
                .build();
    }

    public boolean addUsersToGroup(AddUserToGroupDto addUserToGroupDto) {
        var group = groupRepository.findGroupById(addUserToGroupDto.getGroupId())
                .orElseThrow(() -> new GroupDoesNotExistsException("Invalid Group"));

        var currentUsers = group.getUsers().stream()
                .map(userGroup -> userGroup.getUser().getUsername())
                .collect(Collectors.toSet());

        boolean duplicate = addUserToGroupDto.getUserName().stream().anyMatch(currentUsers::contains);

        if (duplicate) {
            throw new UserAlreadyExistsException("User already part of group.");
        }

        var users = addUserToGroupDto.getUserName()
                .stream().map(id -> userRepository.findUserByUsername(id)
                        .orElseThrow(() -> new UserDoesNotExistsException("User not exists")))
                .toList();

        var userGroups = users.stream()
                .map(user -> UserGroup.builder().user(user)
                        .group(group)
                        .userrole(MEMBER)
                        .build())
                .toList();
        userGroupRepository.saveAll(userGroups);
        log.info("Users added successfully to group.");
        return true;
    }

    @Transactional
    public void removeUsersFromGroup(RemoveUserFromGroupDto dto) throws CannotRemoveUserException {
        var balances = groupBalancesRepo.findBalancesForUsers(dto.getGroupId(), dto.getUserId());
        if (!balances.isEmpty()) {
            var blockedUsers = balances.stream()
                            .flatMap(balance -> Stream.of(
                                            balance.getFrom().getId(),
                                            balance.getTo().getId()))
                            .filter(dto.getUserId()::contains)
                            .collect(Collectors.toSet());
            throw new CannotRemoveUserException("Users have unresolved balances: " + blockedUsers);
        }
        userGroupRepository.deleteByGroupIdAndUserIdIn(dto.getGroupId(), dto.getUserId());
    }

    @Transactional
    public UpdateGroupRequestDto updateGroup(UpdateGroupRequestDto updateGroupRequestDto) {
        var group = groupRepository.findGroupById(updateGroupRequestDto.getGroupId())
                .orElseThrow(() -> new GroupDoesNotExistsException("Group doesn't exists"));
        return updateGroupRequestDto;
    }

    @Transactional
    public void deleteGroup(Long id) throws CannotRemoveGroupException {
        var group = groupRepository.findGroupById(id)
                .orElseThrow(() -> new GroupDoesNotExistsException("Invalid group id"));

        var balanceExists = groupBalancesRepo.findByGroupId(id);
        if (!balanceExists.isEmpty()) {
            throw new CannotRemoveGroupException("Outstanding balances exists, cannot delete group");
        }
        userGroupRepository.deleteByGroupId(id);
        groupRepository.delete(group);
    }

    public GetGroupAndBalances getGroupInfoAndBalances(Long groupId) {
        var group = groupRepository.findGroupById(groupId)
                .orElseThrow(() -> new GroupDoesNotExistsException("Invalid group id"));
        var groupMembers = group.getUsers().stream()
                .map(userGroup -> new UserRecord(userGroup.getUser().getId(),
                        userGroup.getUser().getUsername()))
                .toList();
        var groupInfo = GetGroupAndBalances.builder()
                .balances(findBalances(groupId))
                .groupId(group.getId())
                .groupName(group.getGroupName())
                .memberCount(group.getUsers().size())
                .members(groupMembers)
                .build();
        log.info("Fetched info for group={} balancesSize={}", groupInfo.getGroupName(),
                groupInfo.getBalances().size());
        return groupInfo;
    }

    private List<OwesDto> findBalances(Long groupId) {
        var userId = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userBalancesService.findUserBalancesForGroup((Long) userId, groupId);
    }
}
