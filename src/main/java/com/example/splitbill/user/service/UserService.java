package com.example.splitbill.user.service;

import com.example.splitbill.user.domain.User;
import com.example.splitbill.user.dto.CreateUserResponseDto;
import com.example.splitbill.user.exception.UserAlreadyExistsException;
import com.example.splitbill.user.repo.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public CreateUserResponseDto createNewUser(User user) {
        var userByEmailId = userRepository.findUserByEmailId(user.getEmailId());

        if(userByEmailId.isPresent()) {
            throw new UserAlreadyExistsException("Email id already exists. Please try with different email.");
        }

        var userByMobileNumber = userRepository.findUserByMobileNumber(user.getMobileNumber());
        if(userByMobileNumber.isPresent()) {
            throw new UserAlreadyExistsException("Mobile number already exists. Please try with different number.");
        }

        var savedUser = userRepository.save(user);
        return CreateUserResponseDto.builder()
                .username(savedUser.getUsername())
                .id(savedUser.getId())
                .build();
    }
}
