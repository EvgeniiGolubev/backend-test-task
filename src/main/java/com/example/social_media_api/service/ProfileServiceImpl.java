package com.example.social_media_api.service;

import com.example.social_media_api.domain.dto.UserDto;
import com.example.social_media_api.domain.entity.User;
import com.example.social_media_api.domain.entity.UserSubscription;
import com.example.social_media_api.repository.UserRepository;
import com.example.social_media_api.repository.UserSubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProfileServiceImpl implements ProfileService {
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final UserRepository userRepository;

    @Autowired
    public ProfileServiceImpl(UserSubscriptionRepository userSubscriptionRepository, UserRepository userRepository) {
        this.userSubscriptionRepository = userSubscriptionRepository;
        this.userRepository = userRepository;
    }

    @Override
    public UserDto getUserDto(User user) {
        return new UserDto(user);
    }

    @Override
    public List<UserDto> getUserSubscriptions(User user) {
        return userSubscriptionRepository.findBySubscriber(user).stream()
                .map(sub -> new UserDto(sub.getChannel()))
                .toList();
    }

    @Override
    public List<UserDto> getUserSubscribers(User user) {
        return userSubscriptionRepository.findByChannel(user).stream()
                .map(sub -> new UserDto(sub.getSubscriber()))
                .toList();
    }

    @Override
    public Set<UserDto> getUserFriends(User user) {
        return user.getFriends().stream().map(UserDto::new).collect(Collectors.toSet());
    }

    @Override
    public void changeSubscription(User channel, User subscriber, Boolean subscriptionStatus) {

        if (subscriptionStatus) {
            List<UserSubscription> channelSubscriptions = channel.getSubscribers().stream()
                    .filter(subscription -> subscription.getSubscriber().equals(subscriber))
                    .toList();

            UserSubscription subscriberSubscription = new UserSubscription(channel, subscriber);

            // Если подписка не существует, добавляем новую
            if (channelSubscriptions.isEmpty()) {
                channel.getSubscribers().add(subscriberSubscription);
            }

            UserSubscription channelSubscription = userSubscriptionRepository.findByChannelAndSubscriber(subscriber, channel);

            // Если канал пописан на подписчика, то они становятся друзьями
            if (channelSubscription != null) {
                channelSubscription.setActive(true);
                subscriberSubscription.setActive(true);

                channel.getFriends().add(subscriber);
                subscriber.getFriends().add(channel);
            }
        }  else {
            unfollowAndStopBeingFriends(channel, subscriber);
        }

        userRepository.saveAll(List.of(channel, subscriber));
    }

    @Override
    public void changeSubscriberStatus(User subscriber, User channel, Boolean subscriberStatus) {

        UserSubscription channelSubscription = userSubscriptionRepository.findByChannelAndSubscriber(channel, subscriber);

        // Если подписка активирована, добавляем пользователя в список друзей
        if (subscriberStatus) {
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

        userRepository.saveAll(List.of(channel, subscriber));
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
