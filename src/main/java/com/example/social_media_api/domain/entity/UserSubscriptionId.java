package com.example.social_media_api.domain.entity;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class UserSubscriptionId implements Serializable {
    @Column(name = "channel_id")
    private Long channelId;
    @Column(name = "subscriber_id")
    private Long subscriberId;

    public UserSubscriptionId() {
    }

    public UserSubscriptionId(Long channelId, Long subscriberId) {
        this.channelId = channelId;
        this.subscriberId = subscriberId;
    }

    public Long getChannelId() {
        return channelId;
    }

    public void setChannelId(Long chanelId) {
        this.channelId = chanelId;
    }

    public Long getSubscriberId() {
        return subscriberId;
    }

    public void setSubscriberId(Long subscriberId) {
        this.subscriberId = subscriberId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserSubscriptionId that)) return false;

        if (getChannelId() != null ? !getChannelId().equals(that.getChannelId()) : that.getChannelId() != null)
            return false;
        return getSubscriberId() != null ? getSubscriberId().equals(that.getSubscriberId()) : that.getSubscriberId() == null;
    }

    @Override
    public int hashCode() {
        int result = getChannelId() != null ? getChannelId().hashCode() : 0;
        result = 31 * result + (getSubscriberId() != null ? getSubscriberId().hashCode() : 0);
        return result;
    }
}
