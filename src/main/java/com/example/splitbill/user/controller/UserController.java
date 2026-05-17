package com.example.splitbill.user.controller;

import com.example.splitbill.user.dto.*;
import com.example.splitbill.user.service.UserService;
import com.example.splitbill.user.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/add")
    public UserResponseDto createUser(@RequestBody CreateUserRequestDto createUserRequestDto) {
        var user = User.from(createUserRequestDto);
        return userService.createNewUser(user);
    }

    @PostMapping("/update")
    public UserResponseDto updateUser(@RequestBody UpdateUserDto updateUserDto) {
        return userService.updateUser(updateUserDto);
    }

    @GetMapping("/groups/{userId}")
    public List<GetUserGroupsAndBalancesDto> getUserGroups(@PathVariable Long userId) {
        log.info("Received request to fetch groups for user={}", userId);
        return userService.getUserGroupsAndBalances(userId);
    }

    @GetMapping("/balances/{userId}")
    public Map<UserDto, BigDecimal> getAllBalances(@PathVariable Long userId) {
        log.info("Received request to fetch all balances for user={}", userId);
        return userService.getAllOpenBalances(userId);
    }

    @PostMapping("/settle")
    public Map<UserDto, BigDecimal> settleBalance(@RequestBody SettleBalanceRequestDto requestDto) {
        log.info("Received request to settle balances. request={}", requestDto);
        return userService.recordPaymentForUser(requestDto);
    }

    @PostMapping("/settle/groups")
    public List<GetUserGroupsAndBalancesDto> settleGroupBalance(@RequestBody SettleGroupBalanceRequestDto requestDto) {
        log.info("Received request to settle balance in group. request={}", requestDto);
        return userService.recordPaymentForGroup(requestDto);
    }
}
