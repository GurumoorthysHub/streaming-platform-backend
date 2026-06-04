package com.streamingplatform.service;

import com.streamingplatform.entity.Subscription;
import com.streamingplatform.entity.User;
import com.streamingplatform.repository.SubscriptionRepository;
import com.streamingplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    public Subscription getUserSubscription(Long userId) {
        log.info("Fetching subscription for user: {}", userId);
        return subscriptionRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));
    }

    public Subscription createSubscription(Long userId, Subscription.SubscriptionPlan plan) {
        log.info("Creating subscription for user: {} with plan: {}", userId, plan);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endDate = now.plus(1, ChronoUnit.MONTHS);

        Subscription subscription = Subscription.builder()
                .userId(userId)
                .plan(plan)
                .price(getPriceForPlan(plan))
                .status(Subscription.SubscriptionStatus.ACTIVE)
                .startDate(now)
                .endDate(endDate)
                .renewalDate(endDate)
                .paymentMethod("CREDIT_CARD")
                .build();

        Subscription savedSubscription = subscriptionRepository.save(subscription);
        user.setSubscriptionType(User.SubscriptionType.valueOf(plan.toString()));
        userRepository.save(user);

        return savedSubscription;
    }

    public Subscription upgradeSubscription(Long userId, Subscription.SubscriptionPlan newPlan) {
        log.info("Upgrading subscription for user: {} to plan: {}", userId, newPlan);

        Subscription subscription = subscriptionRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        subscription.setPlan(newPlan);
        subscription.setPrice(getPriceForPlan(newPlan));
        subscription.setStatus(Subscription.SubscriptionStatus.ACTIVE);

        Subscription updatedSubscription = subscriptionRepository.save(subscription);
        User user = userRepository.findById(userId).orElseThrow();
        user.setSubscriptionType(User.SubscriptionType.valueOf(newPlan.toString()));
        userRepository.save(user);

        return updatedSubscription;
    }

    public void cancelSubscription(Long userId) {
        log.info("Cancelling subscription for user: {}", userId);
        Subscription subscription = subscriptionRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        subscription.setStatus(Subscription.SubscriptionStatus.CANCELLED);
        subscriptionRepository.save(subscription);

        User user = userRepository.findById(userId).orElseThrow();
        user.setSubscriptionType(User.SubscriptionType.FREE);
        userRepository.save(user);
    }

    private BigDecimal getPriceForPlan(Subscription.SubscriptionPlan plan) {
        return switch (plan) {
            case FREE -> BigDecimal.ZERO;
            case BASIC -> new BigDecimal("4.99");
            case PREMIUM -> new BigDecimal("9.99");
            case FAMILY -> new BigDecimal("14.99");
        };
    }
}