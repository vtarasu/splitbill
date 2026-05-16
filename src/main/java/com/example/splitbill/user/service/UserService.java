package com.example.splitbill.user.service;

import com.example.splitbill.expense.domain.GroupBalances;
import com.example.splitbill.expense.repo.GroupBalancesRepo;
import com.example.splitbill.group.domain.UserGroup;
import com.example.splitbill.user.domain.User;
import com.example.splitbill.user.dto.*;
import com.example.splitbill.user.exception.UserAlreadyExistsException;
import com.example.splitbill.user.exception.UserDoesNotExistsException;
import com.example.splitbill.user.repo.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final GroupBalancesRepo groupBalancesRepo;

    public UserService(UserRepository userRepository, GroupBalancesRepo groupBalancesRepo) {
        this.userRepository = userRepository;
        this.groupBalancesRepo = groupBalancesRepo;
    }

    public UserResponseDto createNewUser(User user) {
        var userByEmailId = userRepository.findUserByEmailId(user.getEmailId());

        if (userByEmailId.isPresent()) {
            throw new UserAlreadyExistsException("Email id already exists. Please try with different email.");
        }

        var userByMobileNumber = userRepository.findUserByMobileNumber(user.getMobileNumber());
        if (userByMobileNumber.isPresent()) {
            throw new UserAlreadyExistsException("Mobile number already exists. Please try with different number.");
        }

        var savedUser = userRepository.save(user);
        return UserResponseDto.builder()
                .username(savedUser.getUsername())
                .id(savedUser.getId())
                .build();
    }

    @Transactional
    public UserResponseDto updateUser(UpdateUserDto updateUserDto) {
        var user = userRepository.findUserById(updateUserDto.getId())
                .orElseThrow(() -> new UserDoesNotExistsException("User doesn't exists"));

        if (Objects.nonNull(updateUserDto.getEmailId())) {
            user.setEmailId(updateUserDto.getEmailId());
        }

        if (Objects.nonNull(updateUserDto.getMobileNumber())) {
            user.setMobileNumber(updateUserDto.getMobileNumber());
        }

        if (Objects.nonNull(updateUserDto.getUsername())) {
            user.setUsername(updateUserDto.getUsername());
        }
        return UserResponseDto.builder()
                .username(user.getUsername())
                .id(user.getId())
                .build();
    }

    public List<GetUserGroupsAndBalancesDto> getUserGroupsAndBalances(Long userId) {
        var user = userRepository.findUserById(userId)
                .orElseThrow(() -> new UserDoesNotExistsException("User doesn't exists"));

        var userGroups = user.getUserGroups();
        var result = new ArrayList<GetUserGroupsAndBalancesDto>();
        for (UserGroup userGroup : userGroups) {
            var balances = findBalancesForGroup(userGroup);
            var userGroupAndBalance = GetUserGroupsAndBalancesDto.builder()
                    .balances(balances)
                    .groupId(userGroup.getGroup().getId())
                    .groupName(userGroup.getGroup().getGroupName())
                    .build();
            result.add(userGroupAndBalance);
        }
        return result;
    }

    private Map<OwesDto, BigDecimal> findBalancesForGroup(UserGroup userGroup) {
        var groupId = userGroup.getGroup().getId();
        var balances = groupBalancesRepo.findByGroupId(groupId);
        var result = new HashMap<OwesDto, BigDecimal>();
        for (var balance : balances) {
            result.put(new OwesDto(balance.getFrom().getUsername(), balance.getTo().getUsername()),
                    balance.getBalance());
        }
        return result;
    }

    public Map<UserDto, BigDecimal> getAllOpenBalances(Long userId) {
        var groupBalances = groupBalancesRepo.findByFromIdOrToId(userId, userId);
        var allBalances = new HashMap<Long, BigDecimal>();
        var userCache = new HashMap<Long, String>();
        Long id;
        for (var groupBalance : groupBalances) {
            if (groupBalance.getFrom().getId().equals(userId)) {
                id = groupBalance.getTo().getId();
                allBalances.put(id, allBalances.getOrDefault(id, BigDecimal.ZERO).subtract(groupBalance.getBalance()));
                userCache.put(id, groupBalance.getTo().getUsername());
            } else {
                id = groupBalance.getFrom().getId();
                allBalances.put(id, allBalances.getOrDefault(id, BigDecimal.ZERO).add(groupBalance.getBalance()));
                userCache.put(id, groupBalance.getFrom().getUsername());
            }
        }
        return allBalances.entrySet().stream()
                .collect(Collectors.toMap
                        ( entry -> new UserDto(entry.getKey(), userCache.get(entry.getKey())),
                                Map.Entry::getValue));
    }
}
