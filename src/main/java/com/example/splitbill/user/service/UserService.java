package com.example.splitbill.user.service;

import com.example.splitbill.user.domain.User;
import com.example.splitbill.user.dto.*;
import com.example.splitbill.user.exception.InvalidCredentialsException;
import com.example.splitbill.user.exception.UserAlreadyExistsException;
import com.example.splitbill.user.exception.UserDoesNotExistsException;
import com.example.splitbill.user.repo.SettlementsRepository;
import com.example.splitbill.user.repo.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final SettlementsRepository settlementsRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository, SettlementsRepository settlementsRepository,
                       PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.settlementsRepository = settlementsRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public UserResponseDto createNewUser(CreateUserRequestDto requestDto) {
        var user = User.from(requestDto);
        var userByEmailId = userRepository.findUserByEmailId(user.getEmailId());
        var userByName = userRepository.findUserByUsername(user.getUsername());

        if (userByEmailId.isPresent()) {
            throw new UserAlreadyExistsException("Email id already exists. Please try with different email.");
        }

        if (userByName.isPresent()) {
            throw new UserAlreadyExistsException("User name already exists. Please try with different username.");
        }

        var userByMobileNumber = userRepository.findUserByMobileNumber(user.getMobileNumber());
        if (userByMobileNumber.isPresent()) {
            throw new UserAlreadyExistsException("Mobile number already exists. Please try with different number.");
        }

        user.setPassword(passwordEncoder.encode(requestDto.getPassword()));
        var savedUser = userRepository.save(user);
        var token = jwtService.generateToken(user);
        return UserResponseDto.builder()
                .token(token)
                .username(savedUser.getUsername())
                .id(savedUser.getId())
                .build();
    }

    @Transactional
    public UserResponseDto updateUser(UpdateUserDto updateUserDto) {
        var userId = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        var user = userRepository.findUserById((Long) userId)
                .orElseThrow(() -> new UserDoesNotExistsException("User doesn't exists"));

        if (Objects.nonNull(updateUserDto.getEmailId())) {
            user.setEmailId(updateUserDto.getEmailId());
        }

        if (Objects.nonNull(updateUserDto.getMobileNumber())) {
            user.setMobileNumber(updateUserDto.getMobileNumber());
        }

        userRepository.save(user);
        return UserResponseDto.builder()
                .username(user.getUsername())
                .id(user.getId())
                .build();
    }

    public UserResponseDto validate(LoginRequestDto loginRequestDto) {
        var user = userRepository.findUserByUsername(loginRequestDto.getUsername())
                .orElseThrow(() -> new UserDoesNotExistsException("User name doesn't exists. Please try with valid username."));

        boolean passwordMatch = passwordEncoder.matches(loginRequestDto.getPassword(), user.getPassword());

        if (!passwordMatch) {
            throw new InvalidCredentialsException("Invalid username/password");
        }

        var token = jwtService.generateToken(user);
        return UserResponseDto.builder()
                .token(token)
                .id(user.getId())
                .username(user.getUsername())
                .build();
    }

    public UserResponseDto getUser(long userId) {
        var user = userRepository.findUserById(userId)
                .orElseThrow(() -> new UserDoesNotExistsException("User doesn't exists. Please try with valid token."));
        return UserResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .emailId(user.getEmailId())
                .mobileNumber(user.getMobileNumber())
                .build();
    }

    public SettlementHistoryResponseDto getSettlements(Long userId, Integer pageNumber, Integer pageSize) {
        var pageable = PageRequest.of(pageNumber, pageSize, Sort.by("settledAt").descending());
        var settlements = settlementsRepository.findByFromIdOrToId(userId, userId, pageable);
        var totalAmount = settlementsRepository.getTotalSettledAmount(userId);
        var settlementDtos = settlements.stream()
                .map(settlement -> SettlementDto.builder()
                        .id(settlement.getId())
                        .from(settlement.getFrom().getUsername())
                        .to(settlement.getTo().getUsername())
                        .amount(settlement.getAmount())
                        .settledAt(settlement.getSettledAt())
                        .build())
                .toList();
        return SettlementHistoryResponseDto.builder()
                .totalPages(settlements.getTotalPages())
                .totalSettlements(settlements.getNumberOfElements())
                .totalAmount(totalAmount)
                .currentPage(pageNumber)
                .settlements(settlementDtos)
                .build();
    }

    public void updatePasswordForUser(Long userId, UpdatePasswordDto updatePasswordDto) {
        var user = userRepository.findUserById(userId)
                .orElseThrow(() -> new UserDoesNotExistsException("User doesn't exists"));

        boolean passwordMatch = passwordEncoder.matches(updatePasswordDto.getOldPassword(), user.getPassword());

        if (!passwordMatch) {
            throw new InvalidCredentialsException("Incorrect password. Please try with correct password");
        }

        var newPassword = passwordEncoder.encode(updatePasswordDto.getNewPassword());
        user.setPassword(newPassword);
        userRepository.save(user);
    }
}
