package com.example.splitbill.user.controller;

import com.example.splitbill.user.dto.*;
import com.example.splitbill.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @PostMapping("/register")
    public UserResponseDto createUser(@RequestBody CreateUserRequestDto createUserRequestDto) {
        log.info("Received request to register user. username={} emailId={}", createUserRequestDto.getUsername(),
                createUserRequestDto.getEmailId());
        var user = userService.createNewUser(createUserRequestDto);
        log.info("User registered successfully. username={}", user.getUsername());
        return user;
    }

    @PostMapping("/login")
    public UserResponseDto loginUser(@RequestBody LoginRequestDto loginRequestDto) {
        log.info("Received request to login for user. username={}", loginRequestDto.getUsername());
        var user = userService.validate(loginRequestDto);
        log.info("User login successful. username={}", user.getUsername());
        return user;
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
        var userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("Received request to settle balances. userId={} request={}", userId, requestDto);
        return userService.recordPaymentForUser(userId, requestDto);
    }

    @PostMapping("/settle/groups")
    public List<GetUserGroupsAndBalancesDto> settleGroupBalance(@RequestBody SettleGroupBalanceRequestDto requestDto) {
        log.info("Received request to settle balance in group. request={}", requestDto);
        return userService.recordPaymentForGroup(requestDto);
    }
}
