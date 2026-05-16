package com.example.splitbill.user.service;

import com.example.splitbill.expense.repo.GroupBalancesRepo;
import com.example.splitbill.group.domain.UserGroup;
import com.example.splitbill.user.domain.User;
import com.example.splitbill.user.dto.GetUserGroupsAndBalancesDto;
import com.example.splitbill.user.dto.UserResponseDto;
import com.example.splitbill.user.dto.UpdateUserDto;
import com.example.splitbill.user.exception.UserAlreadyExistsException;
import com.example.splitbill.user.exception.UserDoesNotExistsException;
import com.example.splitbill.user.repo.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

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
        for(UserGroup userGroup : userGroups) {
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

    private Map<String, BigDecimal> findBalancesForGroup(UserGroup userGroup) {
        var groupId = userGroup.getGroup().getId();
        var userId = userGroup.getUser().getId();
        var balances = groupBalancesRepo.findByGroupIdAndFromId(groupId, userId);
        balances.addAll(groupBalancesRepo.findByGroupIdAndToId(groupId, userId));
        var result = new HashMap<String, BigDecimal>();
        for(var balance : balances) {
            var user = userId.equals(balance.getFrom().getId()) ? balance.getTo() : balance.getFrom();
            result.put(user.getUsername(), balance.getBalance());
        }
        return result;
    }

    public List<GetUserGroupsAndBalancesDto> getAllOpenBalances(Long userId) {
        var user = userRepository.findUserById(userId)
                .orElseThrow(() -> new UserDoesNotExistsException("User doesn't exists"));
//        var balances = findBalancesForGroup(userGroup);
        return new ArrayList<>();
    }
}
