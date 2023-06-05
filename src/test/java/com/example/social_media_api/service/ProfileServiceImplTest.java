package com.example.social_media_api.service;

import com.example.social_media_api.domain.dto.UserDto;
import com.example.social_media_api.domain.entity.Role;
import com.example.social_media_api.domain.entity.User;
import com.example.social_media_api.domain.entity.UserSubscription;
import com.example.social_media_api.exception.AccessDeniedException;
import com.example.social_media_api.repository.UserSubscriptionRepository;
import com.example.social_media_api.security.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import java.util.*;
import java.util.stream.Collectors;

class ProfileServiceImplTest {
    @Mock
    private UserService userService;
    @Mock
    private UserSubscriptionRepository userSubscriptionRepository;
    @InjectMocks
    private ProfileServiceImpl profileService;
    @Mock
    private UserDetailsImpl authenticatedUser;
    @Mock
    private User userFromDb;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getUserFromUserDetailsImplAndConvertToUserDto() {
        UserDto expectedUserDto = new UserDto(userFromDb);

        when(userService.getUserFromUserDetails(authenticatedUser)).thenReturn(userFromDb);

        UserDto resultUserDto = profileService.getUserDto(authenticatedUser);

        assertEquals(expectedUserDto, resultUserDto);
        verify(userService, times(1)).getUserFromUserDetails(authenticatedUser);
    }

    @Test
    void getUserSubscriptionsFromUserDetailsImplAndConvertToUserDtoList() {
        List<UserSubscription> subscriptions = new ArrayList<>() {{
            add(new UserSubscription(new User(), userFromDb));
            add(new UserSubscription(new User(), userFromDb));
        }};

        List<UserDto> expectedChannelDtos = subscriptions.stream()
                .map(sub -> new UserDto(sub.getChannel()))
                .toList();

        when(userService.getUserFromUserDetails(authenticatedUser)).thenReturn(userFromDb);
        when(userSubscriptionRepository.findBySubscriber(userFromDb)).thenReturn(subscriptions);

        List<UserDto> resultChannelDtos = profileService.getUserSubscriptions(authenticatedUser);

        assertEquals(expectedChannelDtos, resultChannelDtos);

        verify(userService, times(1)).getUserFromUserDetails(authenticatedUser);
        verify(userSubscriptionRepository, times(1)).findBySubscriber(userFromDb);
    }

    @Test
    void getUserSubscribersFromUserDetailsImplAndConvertToUserDtoList() {
        List<UserSubscription> subscribers = new ArrayList<>() {{
            add(new UserSubscription(userFromDb, new User()));
            add(new UserSubscription(userFromDb, new User()));
        }};

        List<UserDto> expectedSubscriberDtos = subscribers.stream()
                .map(sub -> new UserDto(sub.getSubscriber()))
                .toList();

        when(userService.getUserFromUserDetails(authenticatedUser)).thenReturn(userFromDb);
        when(userSubscriptionRepository.findByChannel(userFromDb)).thenReturn(subscribers);

        List<UserDto> resultSubscriberDtos = profileService.getUserSubscribers(authenticatedUser);

        assertEquals(expectedSubscriberDtos, resultSubscriberDtos);
        verify(userService, times(1)).getUserFromUserDetails(authenticatedUser);
        verify(userSubscriptionRepository, times(1)).findByChannel(userFromDb);
    }

    @Test
    void getUserFriendsFromUserDetailsImplAndConvertToUserDtoList() {
        Set<User> friends = new HashSet<>() {{
            add(new User());
            add(new User());
        }};

        Set<UserDto> expectedUserDtos = friends.stream()
                .map(UserDto::new)
                .collect(Collectors.toSet());

        when(userService.getUserFromUserDetails(authenticatedUser)).thenReturn(userFromDb);
        when(userFromDb.getFriends()).thenReturn(friends);

        Set<UserDto> resultUserDtos = profileService.getUserFriends(authenticatedUser);

        assertEquals(expectedUserDtos, resultUserDtos);
        verify(userService, times(1)).getUserFromUserDetails(authenticatedUser);
        verify(userFromDb, times(1)).getFriends();
    }

    @Test
    void subscribeOnChannelAndChannelNotSubscribedOnUser() {
        Long channelId = 1L;
        Boolean isSubscribe = true;

        User channel = new User(
                "channel@mail.ru", "channel",
                "channel", Collections.singleton(Role.USER)
        );
        User subscriber = new User(
                "subscriber@mail.ru", "subscriber",
                "subscriber", Collections.singleton(Role.USER)
        );

        when(userService.findUserById(channelId)).thenReturn(channel);
        when(userService.getUserFromUserDetails(authenticatedUser)).thenReturn(subscriber);
        when(userSubscriptionRepository.findByChannelAndSubscriber(subscriber, channel)).thenReturn(null);

        profileService.changeSubscription(channelId, authenticatedUser, isSubscribe);

        assertTrue(channel.getSubscribers().contains(new UserSubscription(channel, subscriber)));
        assertFalse(channel.getSubscriptions().contains(new UserSubscription(subscriber, channel)));
        assertFalse(channel.getFriends().contains(subscriber));
        assertFalse(subscriber.getFriends().contains(channel));

        verify(userService, times(1)).getUserFromUserDetails(authenticatedUser);
        verify(userService, times(1)).findUserById(channelId);
        verify(userSubscriptionRepository, times(1)).findByChannelAndSubscriber(subscriber, channel);
        verify(userService, times(2)).updateUser(any(User.class));
    }

    @Test
    void subscribeOnChannelAndChannelSubscribedOnUser() {
        Long channelId = 1L;
        Boolean isSubscribe = true;

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

        when(userService.findUserById(channelId)).thenReturn(channel);
        when(userService.getUserFromUserDetails(authenticatedUser)).thenReturn(subscriber);
        when(userSubscriptionRepository.findByChannelAndSubscriber(subscriber, channel)).thenReturn(subscription);

        profileService.changeSubscription(channelId, authenticatedUser, isSubscribe);

        assertTrue(channel.getSubscribers().contains(new UserSubscription(channel, subscriber)));
        assertTrue(channel.getSubscriptions().contains(subscription));
        assertTrue(channel.getFriends().contains(subscriber));
        assertTrue(subscriber.getFriends().contains(channel));

        verify(userService, times(1)).getUserFromUserDetails(authenticatedUser);
        verify(userService, times(1)).findUserById(channelId);
        verify(userSubscriptionRepository, times(1)).findByChannelAndSubscriber(subscriber, channel);
        verify(userService, times(2)).updateUser(any(User.class));
    }

    @Test
    void unsubscribeOnChannelAndChannelNotSubscribedOnUser() {
        Long channelId = 1L;
        Boolean isSubscribe = false;

        User channel = new User(
                "channel@mail.ru", "channel",
                "channel", Collections.singleton(Role.USER)
        );
        User subscriber = new User(
                "subscriber@mail.ru", "subscriber",
                "subscriber", Collections.singleton(Role.USER)
        );

        when(userService.findUserById(channelId)).thenReturn(channel);
        when(userService.getUserFromUserDetails(authenticatedUser)).thenReturn(subscriber);

        profileService.changeSubscription(channelId, authenticatedUser, isSubscribe);

        assertFalse(channel.getSubscribers().contains(new UserSubscription(channel, subscriber)));
        assertFalse(channel.getSubscriptions().contains(new UserSubscription(subscriber, channel)));
        assertFalse(channel.getFriends().contains(subscriber));
        assertFalse(subscriber.getFriends().contains(channel));

        verify(userService, times(1)).getUserFromUserDetails(authenticatedUser);
        verify(userService, times(1)).findUserById(channelId);
        verify(userService, times(2)).updateUser(any(User.class));
    }

    @Test
    void unsubscribeOnChannelAndChannelSubscribedOnUser() {
        Long channelId = 1L;
        Boolean isSubscribe = false;

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

        when(userService.findUserById(channelId)).thenReturn(channel);
        when(userService.getUserFromUserDetails(authenticatedUser)).thenReturn(subscriber);

        profileService.changeSubscription(channelId, authenticatedUser, isSubscribe);

        assertFalse(channel.getSubscribers().contains(new UserSubscription(channel, subscriber)));
        assertTrue(channel.getSubscriptions().contains(subscription));
        assertFalse(channel.getFriends().contains(subscriber));
        assertFalse(subscriber.getFriends().contains(channel));

        verify(userService, times(1)).getUserFromUserDetails(authenticatedUser);
        verify(userService, times(1)).findUserById(channelId);
        verify(userService, times(2)).updateUser(any(User.class));
    }

    @Test
    void changeSubscriptionOnYourselfAndThrowsAccessDeniedException() {
        Long channelId = 1L;
        Boolean isSubscribe = true;

        when(userService.findUserById(channelId)).thenReturn(userFromDb);
        when(userService.getUserFromUserDetails(authenticatedUser)).thenReturn(userFromDb);

        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> profileService.changeSubscription(channelId, authenticatedUser, isSubscribe)
        );

        assertEquals("You can not follow yourself", exception.getMessage());
        verify(userService, never()).updateUser(userFromDb);
    }

    @Test
    void acceptSubscriptionFromSubscriber() {
        Long subscriberId = 1L;
        Boolean status = true;

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

        when(userService.findUserById(subscriberId)).thenReturn(subscriber);
        when(userService.getUserFromUserDetails(authenticatedUser)).thenReturn(channel);
        when(userSubscriptionRepository.findByChannelAndSubscriber(channel, subscriber)).thenReturn(subscription);

        profileService.changeSubscriptionStatus(authenticatedUser, subscriberId, status);

        assertTrue(subscription.isActive());
        assertTrue(subscriber.getSubscribers().contains(new UserSubscription(subscriber, channel)));
        assertTrue(subscriber.getSubscriptions().contains(new UserSubscription(channel, subscriber)));
        assertTrue(subscriber.getFriends().contains(channel));
        assertTrue(channel.getFriends().contains(subscriber));

        verify(userService, times(1)).getUserFromUserDetails(authenticatedUser);
        verify(userService, times(1)).findUserById(subscriberId);
        verify(userService, times(2)).updateUser(any(User.class));
        verify(userSubscriptionRepository, times(1)).findByChannelAndSubscriber(channel, subscriber);
    }

    @Test
    void rejectSubscriptionFromSubscriber() {
        Long subscriberId = 1L;
        Boolean status = false;

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

        when(userService.findUserById(subscriberId)).thenReturn(subscriber);
        when(userService.getUserFromUserDetails(authenticatedUser)).thenReturn(channel);
        when(userSubscriptionRepository.findByChannelAndSubscriber(channel, subscriber)).thenReturn(subscription);

        profileService.changeSubscriptionStatus(authenticatedUser, subscriberId, status);

        assertFalse(subscription.isActive());
        assertTrue(channel.getSubscribers().contains(new UserSubscription(channel, subscriber)));
        assertFalse(channel.getSubscriptions().contains(new UserSubscription(subscriber, channel)));
        assertFalse(channel.getFriends().contains(subscriber));
        assertFalse(subscriber.getFriends().contains(channel));

        verify(userService, times(1)).getUserFromUserDetails(authenticatedUser);
        verify(userService, times(1)).findUserById(subscriberId);
        verify(userService, times(2)).updateUser(any(User.class));
        verify(userSubscriptionRepository, times(1)).findByChannelAndSubscriber(channel, subscriber);
    }

    @Test
    void rejectSubscriptionFromSubscriberIfUsersFriends() {
        Long subscriberId = 1L;
        Boolean status = false;

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

        when(userService.getUserFromUserDetails(authenticatedUser)).thenReturn(channel);
        when(userService.findUserById(subscriberId)).thenReturn(subscriber);
        when(userSubscriptionRepository.findByChannelAndSubscriber(channel, subscriber)).thenReturn(subscription);

        profileService.changeSubscriptionStatus(authenticatedUser, subscriberId, status);

        assertFalse(subscription.isActive());
        assertTrue(channel.getSubscribers().contains(new UserSubscription(channel, subscriber)));
        assertFalse(channel.getSubscriptions().contains(new UserSubscription(subscriber, channel)));
        assertFalse(channel.getFriends().contains(subscriber));
        assertFalse(subscriber.getFriends().contains(channel));

        verify(userService, times(1)).getUserFromUserDetails(authenticatedUser);
        verify(userService, times(1)).findUserById(subscriberId);
        verify(userService, times(2)).updateUser(any(User.class));
        verify(userSubscriptionRepository, times(1)).findByChannelAndSubscriber(channel, subscriber);
    }

    @Test
    void changeSubscriptionStatusForYourselfAndThrowsAccessDeniedException() {
        Long subscriberId = 1L;
        Boolean status = true;

        when(userService.getUserFromUserDetails(authenticatedUser)).thenReturn(userFromDb);
        when(userService.findUserById(subscriberId)).thenReturn(userFromDb);

        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> profileService.changeSubscriptionStatus(authenticatedUser, subscriberId, status)
        );

        assertEquals("You can not follow yourself", exception.getMessage());

        verify(userService, times(1)).getUserFromUserDetails(authenticatedUser);
        verify(userService, times(1)).findUserById(subscriberId);
        verify(userService, never()).updateUser(userFromDb);
    }
}