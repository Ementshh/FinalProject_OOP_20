package com.labubushooter.backend.model;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "players")
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "player_id")
    private UUID playerId;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(name = "total_coins")
    private Integer totalCoins = 0;

    @Column(name = "last_stage")
    private Integer lastStage = 1;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructor
    public Player() {}

    public Player(String username) {
        this.username = username;
    }

    // Getters and Setters
    public UUID getPlayerId() {
        return playerId;
    }

    public void setPlayerId(UUID playerId) {
        this.playerId = playerId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getTotalCoins() {
        return totalCoins;
    }

    public void setTotalCoins(Integer totalCoins) {
        this.totalCoins = totalCoins;
    }

    public Integer getLastStage() {
        return lastStage;
    }

    public void setLastStage(Integer lastStage) {
        this.lastStage = lastStage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @PreUpdate
    public void setLastUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}