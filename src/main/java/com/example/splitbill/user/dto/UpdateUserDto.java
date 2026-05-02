package com.example.splitbill.user.dto;

import lombok.*;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserDto {
    @NonNull
    private Long id;

    private String username;
    private String emailId;
    private String mobileNumber;
}
