package com.example.splitbill.user.service;

import com.example.splitbill.user.domain.User;
import com.example.splitbill.user.dto.UserResponseDto;
import com.example.splitbill.user.dto.UpdateUserDto;
import com.example.splitbill.user.exception.UserAlreadyExistsException;
import com.example.splitbill.user.exception.UserDoesNotExistsException;
import com.example.splitbill.user.repo.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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
}
