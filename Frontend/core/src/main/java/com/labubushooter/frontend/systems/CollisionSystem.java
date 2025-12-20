package com.labubushooter.frontend.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.labubushooter.frontend.objects.Bullet;
import com.labubushooter.frontend.objects.Coin;
import com.labubushooter.frontend.objects.CommonEnemy;
import com.labubushooter.frontend.objects.EnemyBullet;
import com.labubushooter.frontend.objects.FinalBoss;
import com.labubushooter.frontend.objects.MiniBossEnemy;
import com.labubushooter.frontend.objects.Player;

/**
 * Centralized collision detection system for handling all combat collisions.
 * 
 * SOLID Principles Applied:
 * - Single Responsibility: Only handles collision detection and response
 * - Open/Closed: New collision types can be added without modifying existing code
 * - Interface Segregation: Focused collision methods for each collision type
 * 
 * Design Pattern: System Pattern (ECS-inspired)
 * - Separates collision logic from entity logic
 * - Provides centralized collision detection for all game entities
 */
public class CollisionSystem {
    
    // ==================== SINGLETON INSTANCE ====================
    private static CollisionSystem instance;
    
    private CollisionSystem() {
        // Private constructor for singleton
    }
    
    /**
     * Gets the singleton instance of CollisionSystem.
     * Implements Singleton Pattern for centralized collision management.
     * 
     * @return The CollisionSystem instance
     */
    public static CollisionSystem getInstance() {
        if (instance == null) {
            instance = new CollisionSystem();
        }
        return instance;
    }
    
    // ==================== BULLET-ENEMY COLLISIONS ====================
    
    /**
     * Checks and handles collisions between player bullets and common enemies.
     * Applies damage to enemies and removes bullets on contact.
     * 
     * @param enemies Array of active enemies
     * @param bullets Array of active player bullets
     * @param bulletPool Pool to return freed bullets
     */
    public void checkBulletEnemyCollisions(Array<CommonEnemy> enemies, 
                                           Array<Bullet> bullets,
                                           Pool<Bullet> bulletPool) {
        for (int i = enemies.size - 1; i >= 0; i--) {
            CommonEnemy enemy = enemies.get(i);
            
            for (int j = bullets.size - 1; j >= 0; j--) {
                Bullet bullet = bullets.get(j);
                
                if (enemy.collider.overlaps(bullet.bounds)) {
                    enemy.takeDamage(bullet.damage);
                    bullets.removeIndex(j);
                    bulletPool.free(bullet);
                    Gdx.app.log("Combat", "Enemy hit! Health: " + enemy.health);
                    break;
                }
            }
        }
    }
    
    // ==================== BULLET-BOSS COLLISIONS ====================
    
    /**
     * Checks and handles collisions between player bullets and boss enemies.
     * Applies damage to bosses and removes bullets on contact.
     * 
     * @param miniBoss Mini boss enemy (can be null)
     * @param finalBoss Final boss enemy (can be null)
     * @param bullets Array of active player bullets
     * @param bulletPool Pool to return freed bullets
     */
    public void checkBulletBossCollisions(MiniBossEnemy miniBoss, FinalBoss finalBoss,
                                          Array<Bullet> bullets, Pool<Bullet> bulletPool) {
        // Mini boss collision
        if (miniBoss != null && !miniBoss.isDead()) {
            for (int j = bullets.size - 1; j >= 0; j--) {
                Bullet bullet = bullets.get(j);
                
                if (bullet.bounds.overlaps(miniBoss.collider)) {
                    miniBoss.takeDamage(bullet.damage);
                    bullets.removeIndex(j);
                    bulletPool.free(bullet);
                    Gdx.app.log("Combat", "Mini Boss hit! Health: " + miniBoss.health);
                    break;
                }
            }
        }
        
        // Final boss collision
        if (finalBoss != null && !finalBoss.isDead()) {
            for (int j = bullets.size - 1; j >= 0; j--) {
                Bullet bullet = bullets.get(j);
                
                if (bullet.bounds.overlaps(finalBoss.collider)) {
                    finalBoss.takeDamage(bullet.damage);
                    bullets.removeIndex(j);
                    bulletPool.free(bullet);
                    Gdx.app.log("Combat", "Boss hit! Health: " + finalBoss.health);
                    break;
                }
            }
        }
    }
    
    // ==================== ENEMY BULLET-PLAYER COLLISIONS ====================
    
    /**
     * Checks and handles collisions between enemy bullets and player.
     * Applies damage to player and removes bullets on contact.
     * 
     * @param player The player entity
     * @param enemyBullets Array of active enemy bullets
     * @param enemyBulletPool Pool to return freed bullets
     */
    public void checkEnemyBulletPlayerCollisions(Player player,
                                                  Array<EnemyBullet> enemyBullets,
                                                  Pool<EnemyBullet> enemyBulletPool) {
        for (int i = enemyBullets.size - 1; i >= 0; i--) {
            EnemyBullet eb = enemyBullets.get(i);
            
            if (eb.bounds.overlaps(player.bounds)) {
                player.takeDamage(eb.damage);
                enemyBullets.removeIndex(i);
                enemyBulletPool.free(eb);
                Gdx.app.log("Combat", "Player hit by enemy bullet!");
            }
        }
    }
    
    // ==================== COIN-PLAYER COLLISIONS ====================
    
    /**
     * Checks and handles collisions between coins and player.
     * Collects coins and updates score.
     * 
     * @param player The player entity
     * @param coins Array of active coins
     * @param coinPool Pool to return freed coins
     * @return Number of coins collected this frame
     */
    public int checkCoinPlayerCollisions(Player player, Array<Coin> coins, Pool<Coin> coinPool) {
        int coinsCollected = 0;
        
        for (int i = coins.size - 1; i >= 0; i--) {
            Coin coin = coins.get(i);
            
            if (coin.isColliding(player.bounds)) {
                coin.active = false;
                coinsCollected++;
                coins.removeIndex(i);
                coinPool.free(coin);
                Gdx.app.log("Coin", "Collected!");
            }
        }
        
        return coinsCollected;
    }
    
    // ==================== ALL COLLISIONS ====================
    
    /**
     * Result container for collision checks.
     * Contains information about what happened during collision detection.
     */
    public static class CollisionResult {
        public int coinsCollected = 0;
        public boolean playerHit = false;
        public int enemiesHit = 0;
        public boolean bossHit = false;
    }
}
