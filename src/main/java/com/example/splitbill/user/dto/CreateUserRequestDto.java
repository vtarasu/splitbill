package com.example.splitbill.user.dto;

import lombok.*;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserRequestDto {
    @NonNull
    private String username;

    @NonNull
    private String emailId;

    @NonNull
    private String mobileNumber;
}
