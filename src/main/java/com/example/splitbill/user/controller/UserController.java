package com.example.splitbill.user.controller;

import com.example.splitbill.user.dto.*;
import com.example.splitbill.user.exception.UserAlreadyExistsException;
import com.example.splitbill.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequestDto createUserRequestDto) {
        log.info("Received request to register user. username={} emailId={}", createUserRequestDto.getUsername(),
                createUserRequestDto.getEmailId());
        try {
            var user = userService.createNewUser(createUserRequestDto);
            log.info("User registered successfully. username={}", user.getUsername());
            return ResponseEntity.ok(user);
        } catch (UserAlreadyExistsException e) {
            log.error("Invalid request for registering as new user. request={}", createUserRequestDto);
            var errorResponse = ErrorResponse.builder()
                    .status(200)
                    .errorMessage(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
            return ResponseEntity.ok(errorResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Registration request failed");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequestDto loginRequestDto) {
        log.info("Received request to login for user. username={}", loginRequestDto.getUsername());
        try {
            var user = userService.validate(loginRequestDto);
            log.info("User login successful. username={}", loginRequestDto.getUsername());
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("Invalid credentials for username={}", loginRequestDto.getUsername());
            var errorResponse = ErrorResponse.builder()
                    .status(401)
                    .errorMessage("Invalid username or password")
                    .timestamp(LocalDateTime.now())
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(errorResponse);
        }
    }

    @PostMapping("/update")
    public UserResponseDto updateUser(@RequestBody UpdateUserDto updateUserDto) {
        return userService.updateUser(updateUserDto);
    }

    @GetMapping("/me")
    public UserResponseDto getUser() {
        var auth = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .orElseThrow(() -> new RuntimeException("Invalid token. user details not found"));
        long userId = (long) auth.getPrincipal();
        return userService.getUser(userId);
    }

    @GetMapping("/groups/{userId}")
    public List<GetUserGroupsAndBalancesDto> getUserGroups(@PathVariable Long userId) {
        log.info("Received request to fetch groups for user={}", userId);
        return userService.getUserGroupsAndBalances(userId);
    }

    @GetMapping("/balances")
    public List<TotalBalancesDto> getAllBalances() {
        var auth = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .orElseThrow(() -> new RuntimeException("Invalid token. user details not found"));
        long userId = (long) auth.getPrincipal();
        log.info("Received request to fetch all balances for user={}", userId);
        return userService.getAllOpenBalances(4L);
    }

    @PostMapping("/settle")
    public ResponseEntity<?> settleBalance(@RequestBody SettleBalanceRequestDto requestDto) {
        log.info("Received request to settle balances. request={}", requestDto);
        try {
            return ResponseEntity.ok(userService.recordPaymentForUser(requestDto));
        } catch (Exception e) {
            log.error("Error occurred while settling balance. request={}", requestDto, e);
            var errorResponse = ErrorResponse.builder()
                    .status(500)
                    .errorMessage("Error occurred")
                    .timestamp(LocalDateTime.now())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }

    @PostMapping("/settle/groups")
    public List<GetUserGroupsAndBalancesDto> settleGroupBalance(@RequestBody SettleGroupBalanceRequestDto requestDto) {
        log.info("Received request to settle balance in group. request={}", requestDto);
        return userService.recordPaymentForGroup(requestDto);
    }
}
