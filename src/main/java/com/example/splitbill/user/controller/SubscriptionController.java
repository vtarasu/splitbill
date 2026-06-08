package com.example.splitbill.user.controller;

import com.example.splitbill.user.dto.SubscriptionRequestDto;
import com.example.splitbill.user.service.SubscriptionService;
import com.stripe.exception.StripeException;
import com.stripe.model.Subscription;
import com.stripe.param.SubscriptionUpdateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
public class SubscriptionController {
    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @PostMapping("/stripe/customer")
    public ResponseEntity<?> createCustomerAndSetupIntent() {
        try {
            var userId = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            var customerId = subscriptionService.createCustomerId((Long) userId);
            log.info("Stripe customerId retrieved successfully for userId={}", userId);
            return ResponseEntity.ok(customerId);
        } catch (Exception e ) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }

    @PostMapping("/stripe/setup-intent")
    public ResponseEntity<?> createSetupIntent() {
        try {
            var userId = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            var intentId = subscriptionService.setupIntent((Long) userId);
            log.info("Stripe intent created successfully for userId={}", userId);
            return ResponseEntity.ok(intentId);
        } catch (Exception e ) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }

    @PostMapping("/stripe/subscribe")
    public ResponseEntity<?> subscribe(
            @RequestBody SubscriptionRequestDto requestDto) throws StripeException {
        try {
            var userId = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            var subscriptionId = subscriptionService.createSubscription((Long) userId, requestDto);
            log.info("Stripe subscription created successfully for userId={}", userId);
            return ResponseEntity.ok(subscriptionId);
        } catch (Exception e ) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }

    @PostMapping("/cancel")
    public ResponseEntity<String> cancel(
            @RequestBody Map<String, String> body) throws StripeException {

        Subscription sub = Subscription.retrieve(body.get("subscriptionId"));
        sub.update(SubscriptionUpdateParams.builder()
                .setCancelAtPeriodEnd(true).build());

        return ResponseEntity.ok("Cancellation scheduled");
    }

    @PostMapping("/api/subscriptions/webhook")
    public ResponseEntity<String> webhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sig) {
        log.info("Received event from stripe webhook");
        try {
            subscriptionService.processWebHookEvent(payload, sig);
            return ResponseEntity.ok("SUCCESS");
        } catch (Exception e ) {
            log.error("Error occurred processing web hook event", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }

    }
}
