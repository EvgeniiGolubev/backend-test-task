package com.example.social_media_api.domain.entity;

import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="email", nullable = false)
    private String email;

    @Column(name="password", nullable = false)
    private String password;

    @Column(name="name", nullable = false)
    private String name;

    @ElementCollection(targetClass = Role.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    private Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "subscriber", orphanRemoval = true, cascade = CascadeType.ALL)
    private Set<UserSubscription> subscriptions = new HashSet<>();

    @OneToMany(mappedBy = "channel", orphanRemoval = true, cascade = CascadeType.ALL)
    private Set<UserSubscription> subscribers = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "user_friends",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "friend_id")
    )
    private Set<User> friends = new HashSet<>();

    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Message> sendMessages;

    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Message> receivedMessages;

    public User() {
    }

    public User(String email, String password, String name, Set<Role> roles) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.roles = roles;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public Set<UserSubscription> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(Set<UserSubscription> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public Set<UserSubscription> getSubscribers() {
        return subscribers;
    }

    public void setSubscribers(Set<UserSubscription> subscribers) {
        this.subscribers = subscribers;
    }

    public Set<User> getFriends() {
        return friends;
    }

    public void setFriends(Set<User> friends) {
        this.friends = friends;
    }

    public List<Message> getSendMessages() {
        return sendMessages;
    }

    public void setSendMessages(List<Message> setMessages) {
        this.sendMessages = setMessages;
    }

    public List<Message> getReceivedMessages() {
        return receivedMessages;
    }

    public void setReceivedMessages(List<Message> receivedMessages) {
        this.receivedMessages = receivedMessages;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;

        if (getId() != null ? !getId().equals(user.getId()) : user.getId() != null) return false;
        if (getEmail() != null ? !getEmail().equals(user.getEmail()) : user.getEmail() != null) return false;
        if (getName() != null ? !getName().equals(user.getName()) : user.getName() != null) return false;
        return getRoles() != null ? getRoles().equals(user.getRoles()) : user.getRoles() == null;
    }

    @Override
    public int hashCode() {
        int result = getId() != null ? getId().hashCode() : 0;
        result = 31 * result + (getEmail() != null ? getEmail().hashCode() : 0);
        result = 31 * result + (getName() != null ? getName().hashCode() : 0);
        result = 31 * result + (getRoles() != null ? getRoles().hashCode() : 0);
        return result;
    }
}
