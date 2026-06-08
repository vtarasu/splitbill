package com.example.splitbill.user.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class SubscriptionResponseDto {
    String subscriptionId;
    String clientSecret;
    String status;
}
