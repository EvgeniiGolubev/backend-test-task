package com.example.social_media_api.domain.entity;

import javax.persistence.*;

@Entity
@Table(name = "user_subscriptions")
public class UserSubscription {
    @EmbeddedId
    private UserSubscriptionId id;

    @MapsId("channelId")
    @ManyToOne
    private User channel;

    @MapsId("subscriberId")
    @ManyToOne
    private User subscriber;

    private boolean active;

    public UserSubscription() {
    }

    public UserSubscription(User channel, User subscriber) {
        this.channel = channel;
        this.subscriber = subscriber;
        this.id = new UserSubscriptionId(channel.getId(), subscriber.getId());
    }

    public UserSubscriptionId getId() {
        return id;
    }

    public void setId(UserSubscriptionId id) {
        this.id = id;
    }

    public User getChannel() {
        return channel;
    }

    public void setChannel(User chanel) {
        this.channel = chanel;
    }

    public User getSubscriber() {
        return subscriber;
    }

    public void setSubscriber(User subscriber) {
        this.subscriber = subscriber;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserSubscription that)) return false;

        return getId() != null ? getId().equals(that.getId()) : that.getId() == null;
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }
}
