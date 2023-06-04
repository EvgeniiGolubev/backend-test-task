package com.example.social_media_api.service;

import com.example.social_media_api.domain.dto.UserDto;
import com.example.social_media_api.domain.entity.User;
import com.example.social_media_api.domain.entity.UserSubscription;
import com.example.social_media_api.exception.AccessDeniedException;
import com.example.social_media_api.exception.UserNotFoundException;
import com.example.social_media_api.repository.UserSubscriptionRepository;
import com.example.social_media_api.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProfileServiceImpl implements ProfileService {
    private final UserService userService;
    private final UserSubscriptionRepository userSubscriptionRepository;

    @Autowired
    public ProfileServiceImpl(UserService userService, UserSubscriptionRepository userSubscriptionRepository) {
        this.userService = userService;
        this.userSubscriptionRepository = userSubscriptionRepository;
    }

    @Override
    public UserDto getUserDto(UserDetailsImpl user) {
        User userFromDb = userService.getUserFromUserDetails(user);
        return new UserDto(userFromDb);
    }

    @Override
    public List<UserDto> getUserSubscriptions(UserDetailsImpl user) {
        User userFromDb = userService.getUserFromUserDetails(user);

        return userSubscriptionRepository.findBySubscriber(userFromDb).stream()
                .map(sub -> new UserDto(sub.getChannel()))
                .toList();
    }

    @Override
    public List<UserDto> getUserSubscribers(UserDetailsImpl user) {
        User userFromDb = userService.getUserFromUserDetails(user);

        return userSubscriptionRepository.findByChannel(userFromDb).stream()
                .map(sub -> new UserDto(sub.getSubscriber()))
                .toList();
    }

    @Override
    public List<UserDto> getUserFriends(UserDetailsImpl user) {
        User userFromDb = userService.getUserFromUserDetails(user);
        return userFromDb.getFriends().stream()
                .map(UserDto::new)
                .toList();
    }

    @Override
    public void changeSubscription(Long id, UserDetailsImpl user, Boolean isSubscribe)
            throws UserNotFoundException, AccessDeniedException {

        User channel = userService.findUserById(id);
        User subscriber = userService.getUserFromUserDetails(user);

        if (channel.equals(subscriber)) {
            throw new AccessDeniedException("You can not follow yourself");
        }

        if (isSubscribe) {
            List<UserSubscription> channelSubscriptions = channel.getSubscribers().stream()
                    .filter(subscription -> subscription.getSubscriber().equals(subscriber))
                    .toList();

            // Если подписка не существует, добавляем новую
            UserSubscription subscriberSubscription = new UserSubscription(channel, subscriber);
            if (channelSubscriptions.isEmpty()) {
                channel.getSubscribers().add(subscriberSubscription);
            }

            // Если канал пописан на подписчика, то они становятся друзьями
            UserSubscription channelSubscription = userSubscriptionRepository.findByChannelAndSubscriber(subscriber, channel);

            if (channelSubscription != null) {
                channelSubscription.setActive(true);
                subscriberSubscription.setActive(true);

                channel.getFriends().add(subscriber);
                subscriber.getFriends().add(channel);
            }
        }  else {
            unfollowAndStopBeingFriends(channel, subscriber);
        }

        userService.updateUser(channel);
        userService.updateUser(subscriber);
    }

    @Override
    public void changeSubscriptionStatus(UserDetailsImpl user, Long id, Boolean status)
            throws UserNotFoundException, AccessDeniedException {

        User channel = userService.getUserFromUserDetails(user);
        User subscriber = userService.findUserById(id);

        if (channel.equals(subscriber)) {
            throw new AccessDeniedException("You can not follow yourself");
        }

        UserSubscription channelSubscription = userSubscriptionRepository.findByChannelAndSubscriber(channel, subscriber);

        // Если подписка активирована, добавляем пользователя в список друзей
        if (status) {
            channelSubscription.setActive(true);

            List<UserSubscription> subscriptions = subscriber.getSubscribers().stream()
                    .filter(sub -> sub.getSubscriber().equals(channel))
                    .toList();

            // Если подписка на подписчика не существует, создаем новую
            if (subscriptions.isEmpty()) {
                UserSubscription subscription = new UserSubscription(subscriber, channel);
                subscriber.getSubscribers().add(subscription);
                subscription.setActive(true);

                channel.getFriends().add(subscriber);
                subscriber.getFriends().add(channel);
            }
        } else {
            channelSubscription.setActive(false);
            unfollowAndStopBeingFriends(subscriber, channel);
        }

        userService.updateUser(channel);
        userService.updateUser(subscriber);
    }

    private void unfollowAndStopBeingFriends(User channel, User subscriber) {
        List<UserSubscription> subscriptions = channel.getSubscribers().stream()
                .filter(subscription -> subscription.getSubscriber().equals(subscriber))
                .toList();

        subscriptions.forEach(channel.getSubscribers()::remove);

        channel.getFriends().remove(subscriber);
        subscriber.getFriends().remove(channel);
    }
}
