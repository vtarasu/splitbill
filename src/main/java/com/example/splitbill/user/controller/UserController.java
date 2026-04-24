package com.example.splitbill.user.controller;

import com.example.splitbill.user.service.UserService;
import com.example.splitbill.user.domain.User;
import com.example.splitbill.user.dto.CreateUserRequestDto;
import com.example.splitbill.user.dto.CreateUserResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/add")
    public CreateUserResponseDto createUser(@RequestBody CreateUserRequestDto createUserRequestDto) {
        var user = User.from(createUserRequestDto);
        return userService.createNewUser(user);
    }
}
