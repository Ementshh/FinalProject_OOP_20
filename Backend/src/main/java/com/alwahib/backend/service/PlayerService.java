package com.alwahib.backend.service;

import com.alwahib.backend.model.Player;
import com.alwahib.backend.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PlayerService {

    @Autowired
    private PlayerRepository playerRepository;

    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }

    public Optional<Player> getPlayerById(UUID playerId) {
        return playerRepository.findById(playerId);
    }

    public Optional<Player> getPlayerByUsername(String username) {
        return playerRepository.findByUsername(username);
    }

    public Player createPlayer(Player player) {
        if (playerRepository.existsByUsername(player.getUsername())) {
            throw new RuntimeException("Username already exists: " + player.getUsername());
        }
        return playerRepository.save(player);
    }

    public Player updatePlayer(UUID playerId, Player updatedPlayer) {
        Player existingPlayer = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found: " + playerId));

        if (updatedPlayer.getUsername() != null) {
            existingPlayer.setUsername(updatedPlayer.getUsername());
        }
        if (updatedPlayer.getHighScore() != null) {
            existingPlayer.setHighScore(updatedPlayer.getHighScore());
        }
        if (updatedPlayer.getTotalCoins() != null) {
            existingPlayer.setTotalCoins(updatedPlayer.getTotalCoins());
        }
        if (updatedPlayer.getTotalDistanceTravelled() != null) {
            existingPlayer.setTotalDistanceTravelled(updatedPlayer.getTotalDistanceTravelled());
        }

        return playerRepository.save(existingPlayer);
    }

    public void deletePlayer(UUID playerId) {
        if (!playerRepository.existsById(playerId)) {
            throw new RuntimeException("Player not found: " + playerId);
        }
        playerRepository.deleteById(playerId);
    }

    public Player updatePlayerStats(UUID playerId, Integer scoreValue, Integer coinsCollected, Integer distanceTravelled) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found: " + playerId));

        if (scoreValue != null) {
            Integer current = player.getHighScore();
            if (current == null || scoreValue > current) {
                player.setHighScore(scoreValue);
            }
        }
        if (coinsCollected != null) {
            int base = player.getTotalCoins() == null ? 0 : player.getTotalCoins();
            player.setTotalCoins(base + coinsCollected);
        }
        if (distanceTravelled != null) {
            int base = player.getTotalDistanceTravelled() == null ? 0 : player.getTotalDistanceTravelled();
            player.setTotalDistanceTravelled(base + distanceTravelled);
        }

        return playerRepository.save(player);
    }

    public boolean isUsernameExists(String username) {
        return playerRepository.existsByUsername(username);
    }

    public List<Player> getLeaderboardByHighScore(int limit) {
        List<Player> sorted = playerRepository.findAll(Sort.by(Sort.Direction.DESC, "highScore"));
        int toIndex = Math.min(Math.max(limit, 1), sorted.size());
        return new ArrayList<>(sorted.subList(0, toIndex));
    }

    public List<Player> getLeaderboardByTotalCoins() {
        return playerRepository.findAll(Sort.by(Sort.Direction.DESC, "totalCoins"));
    }

    public List<Player> getLeaderboardByTotalDistance() {
        return playerRepository.findAll(Sort.by(Sort.Direction.DESC, "totalDistanceTravelled"));
    }
}