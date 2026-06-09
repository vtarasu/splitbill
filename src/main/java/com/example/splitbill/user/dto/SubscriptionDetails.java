package com.example.splitbill.user.dto;

import com.example.splitbill.user.domain.UserType;
import lombok.*;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class SubscriptionDetails {
    Long userId;
    LocalDate expiryDate;
    UserType userType;
}
