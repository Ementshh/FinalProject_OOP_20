package com.labubushooter.backend.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.labubushooter.backend.model.Player;

@Repository
public interface PlayerRepository extends JpaRepository<Player, UUID> {
    Optional<Player> findByUsername(String username);
    boolean existsByUsername(String username);
}