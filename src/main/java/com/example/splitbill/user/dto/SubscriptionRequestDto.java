package com.example.splitbill.user.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class SubscriptionRequestDto {
    String paymentMethodId;
}
