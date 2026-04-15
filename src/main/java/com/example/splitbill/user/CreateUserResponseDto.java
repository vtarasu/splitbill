package com.example.splitbill.user;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class CreateUserResponseDto {
    private long id;
    private String username;
}
