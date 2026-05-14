package com.example.splitbill.user.controller;

import com.example.splitbill.user.dto.GetUserGroupsAndBalancesDto;
import com.example.splitbill.user.dto.UpdateUserDto;
import com.example.splitbill.user.service.UserService;
import com.example.splitbill.user.domain.User;
import com.example.splitbill.user.dto.CreateUserRequestDto;
import com.example.splitbill.user.dto.UserResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/fetch/groups/{userId}")
    public List<GetUserGroupsAndBalancesDto> getUserGroups(@PathVariable Long userId) {
        log.info("Received request to fetch groups for user={}", userId);
        return userService.getUserGroupsAndBalances(userId);
    }

    @GetMapping("/fetch/balances/{userId}")
    public List<GetUserGroupsAndBalancesDto> getAllBalances(@PathVariable Long userId) {
        log.info("Received request to fetch all balances for user={}", userId);
        return userService.getAllOpenBalances(userId);
    }
}
