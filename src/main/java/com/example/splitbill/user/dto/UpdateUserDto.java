package com.example.splitbill.user.dto;

import lombok.*;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserDto {
    private String emailId;
    private String mobileNumber;
    private String password;
}
