package org.example.myapp.model;

import jakarta.persistence.*;

import java.util.Date;

@Entity
public class ConversationParticipant {
    @Id
    @GeneratedValue
    private Long id;

    @JoinColumn(name = "conversation_id")
    @ManyToOne
    private Conversation conversation;

    @ManyToOne
    private User user;

    private Date joinedAt;
    private Date deletedAt; // soft delete for this user

    private Long lastReadMessageId;

    private Integer unreadCount = 0;  // unread messages for this user


    public Conversation getConversation() {
        return conversation;
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }

    public void setUser(User user) { this.user = user; }

    public User getUser() { return user; }

    public Date getJoinedAt() { return joinedAt; }

    public void setJoinedAt(Date joinedAt) { this.joinedAt = joinedAt; }

    public Date getDeletedAt() { return deletedAt; }

    public Integer getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Integer unreadCount) {
        this.unreadCount = unreadCount;
    }

    public Long getLastReadMessageId() {
        return lastReadMessageId;
    }

    public void setLastReadMessageId(Long lastReadMessageId) {
        this.lastReadMessageId = lastReadMessageId;
    }

    public void setDeletedAt(Date date) { this.deletedAt = date; }
}
