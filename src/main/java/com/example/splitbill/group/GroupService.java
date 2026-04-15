package com.example.splitbill.group;

import com.example.splitbill.user.UserDoesNotExistsException;
import com.example.splitbill.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GroupService {
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    public GroupService(GroupRepository groupRepository, UserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    public CreateGroupResponseDto createGroup(CreateGroupRequestDto createGroupRequestDto) {
        var user = userRepository.findUserById(createGroupRequestDto.getUserId());
        if (user.isEmpty()) {
            throw new UserDoesNotExistsException("User not exists.");
        }

        var existingGroup = groupRepository.findGroupByGroupName(createGroupRequestDto.getGroupName());
        if (existingGroup.isPresent()) {
            throw new GroupAlreadyExistsException(String.format("Group with name : %s  already exists.",
                    createGroupRequestDto.getGroupName()));
        }

        var newGroup = Group.builder()
                .createdBy(user.get())
                .groupName(createGroupRequestDto.getGroupName())
                .groupDescription(createGroupRequestDto.getGroupDescription())
                .build();
        var savedGroup = groupRepository.save(newGroup);
        return CreateGroupResponseDto.from(savedGroup);
    }
}
