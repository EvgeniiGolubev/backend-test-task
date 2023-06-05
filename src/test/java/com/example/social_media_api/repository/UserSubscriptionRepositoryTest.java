package com.example.social_media_api.repository;

import com.example.social_media_api.domain.entity.User;
import com.example.social_media_api.domain.entity.UserSubscription;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class UserSubscriptionRepositoryTest {

    @Mock
    private UserSubscriptionRepository userSubscriptionRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testFindBySubscriber() {
        User subscriber = new User();
        List<UserSubscription> subscriptions = new ArrayList<>();
        when(userSubscriptionRepository.findBySubscriber(subscriber)).thenReturn(subscriptions);

        List<UserSubscription> result = userSubscriptionRepository.findBySubscriber(subscriber);

        assertEquals(subscriptions, result);
    }

    @Test
    public void testFindByChannel() {
        User channel = new User();
        List<UserSubscription> subscriptions = new ArrayList<>();
        when(userSubscriptionRepository.findByChannel(channel)).thenReturn(subscriptions);

        List<UserSubscription> result = userSubscriptionRepository.findByChannel(channel);

        assertEquals(subscriptions, result);
    }

    @Test
    public void testFindByChannelAndSubscriber() {
        User channel = new User();
        User subscriber = new User();
        UserSubscription subscription = new UserSubscription();
        when(userSubscriptionRepository.findByChannelAndSubscriber(channel, subscriber)).thenReturn(subscription);

        UserSubscription result = userSubscriptionRepository.findByChannelAndSubscriber(channel, subscriber);

        assertEquals(subscription, result);
    }
}