package com.example.splitbill.user.dto;

import lombok.*;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequestDto {
    @NonNull
    private String username;

    @NonNull
    private String password;
}
