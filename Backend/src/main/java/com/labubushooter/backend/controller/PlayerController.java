package com.labubushooter.backend.controller;

import com.labubushooter.backend.model.Player;
import com.labubushooter.backend.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/players")
@CrossOrigin(origins = "*")
public class PlayerController {

    @Autowired
    private PlayerService playerService;

    @GetMapping
    public ResponseEntity<List<Player>> getAllPlayers() {
        return ResponseEntity.ok(playerService.getAllPlayers());
    }

    @GetMapping("/{playerId}")
    public ResponseEntity<?> getPlayerById(@PathVariable UUID playerId) {
        Optional<Player> player = playerService.getPlayerById(playerId);
        if (player.isPresent()) return ResponseEntity.ok(player.get());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Player not found");
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginOrCreate(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            if (username == null || username.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username is required");
            }

            Player player = playerService.loginOrCreate(username.trim());
            
            Map<String, Object> response = new HashMap<>();
            response.put("player", player);
            response.put("message", "Login with username: " + username + " Success");
            response.put("isNewPlayer", player.getLastStage() == 1 && player.getTotalCoins() == 0);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/{playerId}/progress")
    public ResponseEntity<?> updateProgress(
            @PathVariable UUID playerId,
            @RequestBody Map<String, Integer> progress) {
        try {
            Integer lastStage = progress.get("lastStage");
            Integer coinsCollected = progress.get("coinsCollected");

            Player updated = playerService.updatePlayerProgress(playerId, lastStage, coinsCollected);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PutMapping("/{playerId}/reset")
    public ResponseEntity<?> resetProgress(@PathVariable UUID playerId) {
        try {
            Player reset = playerService.resetPlayerProgress(playerId);
            return ResponseEntity.ok(reset);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping("/{playerId}")
    public ResponseEntity<?> deletePlayer(@PathVariable UUID playerId) {
        try {
            playerService.deletePlayer(playerId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}