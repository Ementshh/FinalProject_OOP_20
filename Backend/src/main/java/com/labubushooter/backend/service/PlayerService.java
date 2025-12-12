package com.labubushooter.backend.service;

import com.labubushooter.backend.model.Player;
import com.labubushooter.backend.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public Player createPlayer(String username) {
        if (playerRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists: " + username);
        }
        Player player = new Player(username);
        return playerRepository.save(player);
    }

    public Player loginOrCreate(String username) {
        Optional<Player> existingPlayer = playerRepository.findByUsername(username);
        if (existingPlayer.isPresent()) {
            return existingPlayer.get();
        } else {
            Player newPlayer = new Player(username);
            return playerRepository.save(newPlayer);
        }
    }

    public Player updatePlayerProgress(UUID playerId, Integer lastStage, Integer coinsCollected) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found: " + playerId));

        if (lastStage != null) {
            player.setLastStage(lastStage);
        }
        if (coinsCollected != null) {
            int currentCoins = player.getTotalCoins() != null ? player.getTotalCoins() : 0;
            player.setTotalCoins(currentCoins + coinsCollected);
        }

        return playerRepository.save(player);
    }

    public Player resetPlayerProgress(UUID playerId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found: " + playerId));

        player.setLastStage(1);
        // Don't reset total coins - keep lifetime stats
        return playerRepository.save(player);
    }

    public boolean isUsernameExists(String username) {
        return playerRepository.existsByUsername(username);
    }

    public void deletePlayer(UUID playerId) {
        if (!playerRepository.existsById(playerId)) {
            throw new RuntimeException("Player not found: " + playerId);
        }
        playerRepository.deleteById(playerId);
    }
}