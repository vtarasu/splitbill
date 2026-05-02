package com.example.splitbill.user.dto;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class UserResponseDto {
    private long id;
    private String username;
}
