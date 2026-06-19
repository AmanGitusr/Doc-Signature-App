package com.docsignature.backend.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "audit_events")
public class AuditEventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    private DocumentEntity document;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_user_id")
    private UserEntity actorUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "actor_type", nullable = false, length = 40)
    private ActorType actorType;

    @Column(nullable = false, length = 80)
    private String action;

    @Column(nullable = false, length = 1000)
    private String details;

    @Column(name = "ip_address", length = 80)
    private String ipAddress;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DocumentEntity getDocument() {
        return document;
    }

    public void setDocument(DocumentEntity document) {
        this.document = document;
    }

    public UserEntity getActorUser() {
        return actorUser;
    }

    public void setActorUser(UserEntity actorUser) {
        this.actorUser = actorUser;
    }

    public ActorType getActorType() {
        return actorType;
    }

    public void setActorType(ActorType actorType) {
        this.actorType = actorType;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
