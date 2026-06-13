package com.example.splitbill.user.service;

import com.example.splitbill.user.domain.UserType;
import com.example.splitbill.user.dto.SubscriptionRequestDto;
import com.example.splitbill.user.dto.SubscriptionResponseDto;
import com.example.splitbill.user.exception.SubscriptionException;
import com.example.splitbill.user.exception.UserDoesNotExistsException;
import com.example.splitbill.user.repo.UserRepository;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.net.Webhook;
import com.stripe.param.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;

@Slf4j
@Service
public class SubscriptionService {
    private final UserRepository userRepository;

    @Value("${stripe.price-id}")
    private String priceId;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    @Value("${stripe.secret-key}")
    private String stripeSecretKey;

    public SubscriptionService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    public String createCustomerId(Long userId) {
        var user = userRepository.findUserById(userId)
                .orElseThrow(() -> new UserDoesNotExistsException("Invalid user id"));

        if (user.getStripeCustomerId() != null) {
            return user.getStripeCustomerId();
        }
        var params = CustomerCreateParams.builder()
                .setEmail(user.getEmailId())
                .setName(user.getUsername())
                .putMetadata("userId", String.valueOf(user.getId()))
                .build();

        try {
            var customer = Customer.create(params);
            user.setStripeCustomerId(customer.getId());
            userRepository.save(user);
            return customer.getId();
        } catch (StripeException e) {
            log.error("Error occurred while creating as stripe customer.", e);
            throw new SubscriptionException("Unable to create customer");
        }
    }

    public String setupIntent(Long userId) {
        var user = userRepository.findUserById(userId)
                .orElseThrow(() -> new UserDoesNotExistsException("Invalid user id"));

        if (Objects.isNull(user.getStripeCustomerId())) {
            throw new SubscriptionException("Stripe Id doesn't exists.");
        }

        SetupIntentCreateParams params = SetupIntentCreateParams.builder()
                .setCustomer(user.getStripeCustomerId())
                .addPaymentMethodType("card")
                .build();
        try {
            SetupIntent intent = SetupIntent.create(params);
            return intent.getClientSecret();
        } catch (StripeException e) {
            log.error("Error occurred while creating stripe intent.", e);
            throw new SubscriptionException("Unable to create intent");
        }
    }

    public SubscriptionResponseDto createSubscription(Long userId, SubscriptionRequestDto requestDto) {
        var user = userRepository.findUserById(userId)
                .orElseThrow(() -> new UserDoesNotExistsException("Invalid user id"));

        if (Objects.isNull(user.getStripeCustomerId())) {
            throw new SubscriptionException("Stripe Id doesn't exists.");
        }

        try {
            var customerId = user.getStripeCustomerId();
            var customer = Customer.retrieve(customerId);
            customer.update(CustomerUpdateParams.builder().setInvoiceSettings(
                            CustomerUpdateParams.InvoiceSettings.builder()
                                    .setDefaultPaymentMethod(requestDto.getPaymentMethodId()).build())
                    .build());

            SubscriptionCollection existing = Subscription.list(
                    SubscriptionListParams.builder()
                            .setCustomer(customerId)
                            .setStatus(SubscriptionListParams.Status.ACTIVE)
                            .build());

            Subscription subscription;
            if (!existing.getData().isEmpty()) {
                subscription = existing.getData().getFirst();
            } else {
                subscription = Subscription.create(SubscriptionCreateParams.builder()
                        .setCustomer(customerId)
                        .addItem(SubscriptionCreateParams.Item.builder()
                                .setPrice(priceId).build())
                        .setPaymentBehavior(
                                SubscriptionCreateParams.PaymentBehavior.DEFAULT_INCOMPLETE)
                        .setDefaultPaymentMethod(requestDto.getPaymentMethodId())
                        .addExpand("latest_invoice.confirmation_secret")
                        .build());
                user.setStripeSubscriptionId(subscription.getId());
                userRepository.save(user);
            }

            var invoice = subscription.getLatestInvoiceObject();
            var clientSecret = invoice.getConfirmationSecret().getClientSecret();
            return SubscriptionResponseDto.builder()
                    .subscriptionId(subscription.getId())
                    .status(subscription.getStatus())
                    .clientSecret(clientSecret)
                    .build();
        } catch (Exception e) {
            log.error("Error occurred while creating subscription", e);
            throw new SubscriptionException("Error occurred while creating subscription");
        }
    }

    public void processWebHookEvent(String payload, String signature) {
        Event event;
        try {
            event = Webhook.constructEvent(payload, signature, webhookSecret);
            log.info("Received eventType={}", event.getType());
        } catch (SignatureVerificationException e) {
            log.error("Bad signature, webhook event rejected");
            return;
        }
        switch (event.getType()) {
            case "customer.subscription.updated" -> {
                Subscription subscription = (Subscription) event.getDataObjectDeserializer()
                        .getObject()
                        .orElseThrow();

                String customerId = subscription.getCustomer();

                var user = userRepository.findUserByStripeCustomerId(customerId)
                        .orElseThrow(() -> new SubscriptionException("Customer ID not found"));

                if (subscription.getCancelAtPeriodEnd()) {
                    user.setUserType(UserType.FREE);
                } else {
                    user.setUserType(UserType.PREMIUM);
                    long periodEnd = subscription.getItems().getData()
                            .getFirst().getCurrentPeriodEnd();
                    LocalDate premiumExpiry = Instant.ofEpochSecond(periodEnd)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();
                    user.setPremiumExpiresAt(premiumExpiry);
                }
                userRepository.save(user);
            }
            case "invoice.payment_failed" -> {
                Invoice invoice = (Invoice) event.getDataObjectDeserializer()
                        .getObject().get();
                String customerId = invoice.getCustomer();
                var user = userRepository.findUserByStripeCustomerId(customerId)
                        .orElseThrow(() -> new SubscriptionException("Customer ID not found"));
                user.setUserType(UserType.FREE);
                userRepository.save(user);
            }
            case "customer.subscription.deleted" -> {
                Subscription sub = (Subscription) event.getDataObjectDeserializer()
                        .getObject().get();
                String customerId = sub.getCustomer();
                var user = userRepository.findUserByStripeCustomerId(customerId)
                        .orElseThrow(() -> new SubscriptionException("Customer ID not found"));
                user.setUserType(UserType.FREE);
                user.setPremiumExpiresAt(null);
                userRepository.save(user);
            }
        }
    }

    public String cancelSubscription(Long userId) {
        var user = userRepository.findUserById(userId)
                .orElseThrow(() -> new UserDoesNotExistsException("Invalid user id"));

        if (Objects.isNull(user.getStripeCustomerId()) || Objects.isNull(user.getStripeSubscriptionId()) ||
                !UserType.PREMIUM.equals(user.getUserType())) {
            throw new SubscriptionException("Invalid subscription cancellation request.");
        }

        Subscription sub = null;
        try {
            sub = Subscription.retrieve(user.getStripeSubscriptionId());
            sub.update(SubscriptionUpdateParams.builder()
                    .setCancelAtPeriodEnd(true).build());
            return user.getStripeSubscriptionId();
        } catch (StripeException e) {
            log.error("Error occurred while cancelling subscription.", e);
            throw new SubscriptionException("Error occurred while cancelling subscription.");
        }
    }
}
