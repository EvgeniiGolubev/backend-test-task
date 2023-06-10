package com.example.social_media_api.service;

import com.example.social_media_api.domain.dto.UserDto;
import com.example.social_media_api.domain.entity.Role;
import com.example.social_media_api.domain.entity.User;
import com.example.social_media_api.domain.entity.UserSubscription;
import com.example.social_media_api.repository.UserRepository;
import com.example.social_media_api.repository.UserSubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import java.util.*;
import java.util.stream.Collectors;

class ProfileServiceImplTest {
    @Mock
    private UserSubscriptionRepository userSubscriptionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProfileServiceImpl profileService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getUserDto() {
        User user = new User();
        UserDto expected = new UserDto(user);

        UserDto resultUserDto = profileService.getUserDto(user);

        assertEquals(expected, resultUserDto);
    }

    @Test
    void getUserSubscriptions() {
        User channel = new User();
        User subscriber = new User();
        List<UserSubscription> subscriptions = new ArrayList<>() {{
            add(new UserSubscription(channel, subscriber));
            add(new UserSubscription(channel, subscriber));
        }};

        List<UserDto> expected = subscriptions.stream()
                .map(sub -> new UserDto(sub.getChannel()))
                .toList();

        when(userSubscriptionRepository.findBySubscriber(subscriber)).thenReturn(subscriptions);

        List<UserDto> result = profileService.getUserSubscriptions(subscriber);

        assertEquals(expected, result);
        verify(userSubscriptionRepository, times(1)).findBySubscriber(subscriber);
    }

    @Test
    void getUserSubscribers() {
        User channel = new User();
        User subscriber = new User();
        List<UserSubscription> subscribers = new ArrayList<>() {{
            add(new UserSubscription(channel, subscriber));
            add(new UserSubscription(channel, subscriber));
        }};

        List<UserDto> expected = subscribers.stream()
                .map(sub -> new UserDto(sub.getSubscriber()))
                .toList();

        when(userSubscriptionRepository.findByChannel(channel)).thenReturn(subscribers);

        List<UserDto> result = profileService.getUserSubscribers(channel);

        assertEquals(expected, result);
        verify(userSubscriptionRepository, times(1)).findByChannel(channel);
    }

    @Test
    void getUserFriendsFromUserDetailsImplAndConvertToUserDtoList() {
        User user = Mockito.mock(User.class);
        Set<User> friends = new HashSet<>() {{
            add(new User());
            add(new User());
        }};

        Set<UserDto> expected = friends.stream()
                .map(UserDto::new)
                .collect(Collectors.toSet());

        when(user.getFriends()).thenReturn(friends);

        Set<UserDto> result = profileService.getUserFriends(user);

        assertEquals(expected, result);
        verify(user, times(1)).getFriends();
    }

    @Test
    void changeSubscriptionTrueAndChannelNotSubscribedOnUser() {
        Boolean subscriptionStatus = true;

        User channel = new User(
                "channel@mail.ru", "channel",
                "channel", Collections.singleton(Role.USER)
        );
        User subscriber = new User(
                "subscriber@mail.ru", "subscriber",
                "subscriber", Collections.singleton(Role.USER)
        );

        when(userSubscriptionRepository.findByChannelAndSubscriber(subscriber, channel)).thenReturn(null);

        profileService.changeSubscription(channel, subscriber, subscriptionStatus);

        assertTrue(channel.getSubscribers().contains(new UserSubscription(channel, subscriber)));
        assertFalse(channel.getSubscriptions().contains(new UserSubscription(subscriber, channel)));
        assertFalse(channel.getFriends().contains(subscriber));
        assertFalse(subscriber.getFriends().contains(channel));
        verify(userSubscriptionRepository, times(1)).findByChannelAndSubscriber(subscriber, channel);
        verify(userRepository, times(1)).saveAll(List.of(channel, subscriber));
    }

    @Test
    void changeSubscriptionTrueAndChannelSubscribedOnUser() {
        Boolean subscriptionStatus = true;

        User channel = new User(
                "channel@mail.ru", "channel",
                "channel", Collections.singleton(Role.USER)
        );
        User subscriber = new User(
                "subscriber@mail.ru", "subscriber",
                "subscriber", Collections.singleton(Role.USER)
        );

        UserSubscription subscription = new UserSubscription(subscriber, channel);
        channel.setSubscriptions(Collections.singleton(subscription));

        when(userSubscriptionRepository.findByChannelAndSubscriber(subscriber, channel)).thenReturn(subscription);

        profileService.changeSubscription(channel, subscriber, subscriptionStatus);

        assertTrue(channel.getSubscribers().contains(new UserSubscription(channel, subscriber)));
        assertTrue(channel.getSubscriptions().contains(subscription));
        assertTrue(channel.getFriends().contains(subscriber));
        assertTrue(subscriber.getFriends().contains(channel));
        verify(userSubscriptionRepository, times(1)).findByChannelAndSubscriber(subscriber, channel);
        verify(userRepository, times(1)).saveAll(List.of(channel, subscriber));
    }

    @Test
    void changeSubscriptionFalseAndChannelNotSubscribedOnUser() {
        Boolean subscriptionStatus = false;

        User channel = new User(
                "channel@mail.ru", "channel",
                "channel", Collections.singleton(Role.USER)
        );
        User subscriber = new User(
                "subscriber@mail.ru", "subscriber",
                "subscriber", Collections.singleton(Role.USER)
        );

        profileService.changeSubscription(channel, subscriber, subscriptionStatus);

        assertFalse(channel.getSubscribers().contains(new UserSubscription(channel, subscriber)));
        assertFalse(channel.getSubscriptions().contains(new UserSubscription(subscriber, channel)));
        assertFalse(channel.getFriends().contains(subscriber));
        assertFalse(subscriber.getFriends().contains(channel));
        verify(userRepository, times(1)).saveAll(List.of(channel, subscriber));
    }

    @Test
    void changeSubscriptionFalseAndChannelSubscribedOnUser() {
        Boolean subscriptionStatus = false;

        User channel = new User(
                "channel@mail.ru", "channel",
                "channel", Collections.singleton(Role.USER)
        );
        User subscriber = new User(
                "subscriber@mail.ru", "subscriber",
                "subscriber", Collections.singleton(Role.USER)
        );

        UserSubscription subscription = new UserSubscription(subscriber, channel);
        channel.setSubscriptions(Collections.singleton(subscription));

        profileService.changeSubscription(channel, subscriber, subscriptionStatus);

        assertFalse(channel.getSubscribers().contains(new UserSubscription(channel, subscriber)));
        assertTrue(channel.getSubscriptions().contains(subscription));
        assertFalse(channel.getFriends().contains(subscriber));
        assertFalse(subscriber.getFriends().contains(channel));
        verify(userRepository, times(1)).saveAll(List.of(channel, subscriber));
    }

    @Test
    void changeSubscriberStatusTrue() {
        Boolean subscriberStatus = true;

        User channel = new User(
                "channel@mail.ru", "channel",
                "channel", Collections.singleton(Role.USER)
        );
        User subscriber = new User(
                "subscriber@mail.ru", "subscriber",
                "subscriber", Collections.singleton(Role.USER)
        );

        UserSubscription subscription = new UserSubscription(channel, subscriber);

        channel.setSubscribers(Collections.singleton(subscription));
        subscriber.setSubscriptions(Collections.singleton(subscription));

        when(userSubscriptionRepository.findByChannelAndSubscriber(channel, subscriber)).thenReturn(subscription);

        profileService.changeSubscriberStatus(subscriber, channel, subscriberStatus);

        assertTrue(subscription.isActive());
        assertTrue(subscriber.getSubscribers().contains(new UserSubscription(subscriber, channel)));
        assertTrue(subscriber.getSubscriptions().contains(new UserSubscription(channel, subscriber)));
        assertTrue(subscriber.getFriends().contains(channel));
        assertTrue(channel.getFriends().contains(subscriber));

        verify(userSubscriptionRepository, times(1)).findByChannelAndSubscriber(channel, subscriber);
        verify(userRepository, times(1)).saveAll(List.of(channel, subscriber));
    }

    @Test
    void changeSubscriberStatusFalse() {
        Boolean subscriberStatus = false;

        User channel = new User(
                "channel@mail.ru", "channel",
                "channel", Collections.singleton(Role.USER)
        );
        User subscriber = new User(
                "subscriber@mail.ru", "subscriber",
                "subscriber", Collections.singleton(Role.USER)
        );

        UserSubscription subscription = new UserSubscription(channel, subscriber);

        channel.setSubscribers(Collections.singleton(subscription));
        subscriber.setSubscriptions(Collections.singleton(subscription));

        when(userSubscriptionRepository.findByChannelAndSubscriber(channel, subscriber)).thenReturn(subscription);

        profileService.changeSubscriberStatus(subscriber, channel, subscriberStatus);

        assertFalse(subscription.isActive());
        assertTrue(channel.getSubscribers().contains(new UserSubscription(channel, subscriber)));
        assertFalse(channel.getSubscriptions().contains(new UserSubscription(subscriber, channel)));
        assertFalse(channel.getFriends().contains(subscriber));
        assertFalse(subscriber.getFriends().contains(channel));

        verify(userSubscriptionRepository, times(1)).findByChannelAndSubscriber(channel, subscriber);
        verify(userRepository, times(1)).saveAll(List.of(channel, subscriber));
    }

    @Test
    void rejectSubscriptionFromSubscriberIfUsersFriends() {
        Boolean subscriberStatus = false;

        User channel = new User(
                "channel@mail.ru", "channel",
                "channel", Collections.singleton(Role.USER)
        );
        User subscriber = new User(
                "subscriber@mail.ru", "subscriber",
                "subscriber", Collections.singleton(Role.USER)
        );

        UserSubscription subscription = new UserSubscription(channel, subscriber);

        channel.getSubscribers().add(subscription);
        subscriber.getSubscriptions().add(subscription);
        channel.getFriends().add(subscriber);
        subscriber.getFriends().add(channel);

        when(userSubscriptionRepository.findByChannelAndSubscriber(channel, subscriber)).thenReturn(subscription);

        profileService.changeSubscriberStatus(subscriber, channel, subscriberStatus);

        assertFalse(subscription.isActive());
        assertTrue(channel.getSubscribers().contains(new UserSubscription(channel, subscriber)));
        assertFalse(channel.getSubscriptions().contains(new UserSubscription(subscriber, channel)));
        assertFalse(channel.getFriends().contains(subscriber));
        assertFalse(subscriber.getFriends().contains(channel));

        verify(userSubscriptionRepository, times(1)).findByChannelAndSubscriber(channel, subscriber);
        verify(userRepository, times(1)).saveAll(List.of(channel, subscriber));
    }
}