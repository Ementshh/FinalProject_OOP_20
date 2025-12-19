package com.labubushooter.frontend.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.TimeUtils;
import com.labubushooter.frontend.GameState;
import com.labubushooter.frontend.core.GameContext;
import com.labubushooter.frontend.objects.Bullet;
import com.labubushooter.frontend.objects.Coin;
import com.labubushooter.frontend.objects.CommonEnemy;
import com.labubushooter.frontend.objects.EnemyBullet;
import com.labubushooter.frontend.objects.Ground;
import com.labubushooter.frontend.objects.Platform;
import com.labubushooter.frontend.objects.Player;
import com.labubushooter.frontend.patterns.ShootingStrategy;

/**
 * Main gameplay screen.
 * Handles game logic, input, and rendering during active gameplay.
 */
public class GamePlayScreen extends BaseScreen {
    
    // Callback interface for Main.java to handle level loading
    public interface GamePlayCallback {
        void loadLevel(int level);
        void saveProgress();
        void restartGame();
    }
    
    private GamePlayCallback callback;
    private boolean needsLevelLoad = true;
    
    public GamePlayScreen(GameContext context) {
        super(context);
    }
    
    public void setCallback(GamePlayCallback callback) {
        this.callback = callback;
    }
    
    @Override
    public void show() {
        super.show();
        // Load level when screen is shown for the first time or after restart
        if (needsLevelLoad && callback != null) {
            callback.loadLevel(context.currentLevel);
            needsLevelLoad = false;
        }
    }
    
    /**
     * Mark that level needs to be loaded when screen is shown.
     */
    public void setNeedsLevelLoad(boolean needs) {
        this.needsLevelLoad = needs;
    }
    
    @Override
    public void handleInput(float delta) {
        // Check if player is dead
        if (context.player.isDead()) {
            transitionTo(GameState.GAME_OVER);
            Gdx.app.log("Game", "GAME OVER");
            return;
        }
        
        // Check for PAUSE
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            transitionTo(GameState.PAUSED);
            Gdx.app.log("Game", "PAUSED");
            return;
        }
        
        // --- SECRET LEVEL SKIP ---
        if (Gdx.input.isKeyJustPressed(Input.Keys.F3) && callback != null) {
            callback.loadLevel(3);
            Gdx.app.log("Debug", "Skipped to Level 3 (Mini Boss)");
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F5) && callback != null) {
            callback.loadLevel(5);
            Gdx.app.log("Debug", "Skipped to Level 5 (Final Boss)");
        }
        
        // --- DEBUG: INSTA-KILL BOSS ---
        if (Gdx.input.isKeyJustPressed(Input.Keys.K)) {
            if (context.currentLevel == 3 && context.miniBoss != null && !context.miniBoss.isDead()) {
                context.miniBoss.takeDamage(999999f);
                Gdx.app.log("Debug", "Mini Boss instantly killed!");
            }
            if (context.currentLevel == 5 && context.boss != null && !context.boss.isDead()) {
                context.boss.takeDamage(999999f);
                Gdx.app.log("Debug", "Final Boss instantly killed!");
            }
        }
        
        // --- WEAPON SWITCHING ---
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            context.player.setWeapon(context.pistolStrategy);
            Gdx.app.log("WeaponSystem", "Pistol Equipped");
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
            context.player.setWeapon(context.mac10Strategy);
            Gdx.app.log("WeaponSystem", "Mac-10 Equipped");
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
            context.player.setWeapon(null);
            Gdx.app.log("WeaponSystem", "Unarmed");
        }
        
        // --- JUMP ---
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            context.player.jump();
        }
        
        // --- MOUSE SHOOTING ---
        ShootingStrategy currentWeapon = context.player.getWeapon();
        if (currentWeapon != null) {
            if (currentWeapon.isAutomatic()) {
                if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
                    context.player.shoot(context.activeBullets, context.bulletPool);
                }
            } else {
                if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                    context.player.shoot(context.activeBullets, context.bulletPool);
                }
            }
        }
    }
    
    @Override
    public void update(float delta) {
        // --- UPDATE PLAYER ---
        context.player.update(delta, context.platforms, context.grounds);
        
        // Update bosses
        if (context.currentLevel == 3 && context.miniBoss != null && !context.miniBoss.isDead()) {
            context.miniBoss.update(delta, context.platforms, context.grounds, context.player);
        }
        
        if (context.currentLevel == 5 && context.boss != null && !context.boss.isDead()) {
            context.boss.update(delta, context.platforms, context.grounds, context.player, 
                               context.activeEnemyBullets, context.enemyBulletPool);
        }
        
        // Update enemy bullets
        for (int i = context.activeEnemyBullets.size - 1; i >= 0; i--) {
            EnemyBullet eb = context.activeEnemyBullets.get(i);
            eb.update(delta);
            
            if (eb.isOutOfBounds(context.currentLevelWidth, context.viewport.getWorldHeight())) {
                context.activeEnemyBullets.removeIndex(i);
                context.enemyBulletPool.free(eb);
            }
        }
        
        // Spawn enemy based on timer - WITH LIMIT
        if (context.currentLevel != 3 && context.currentLevel != 5) {
            if (TimeUtils.nanoTime() - context.lastEnemySpawnTime > context.nextEnemySpawnDelay) {
                int maxEnemies = context.getMaxEnemiesForLevel(context.currentLevel);
                
                if (context.activeEnemies.size < maxEnemies) {
                    spawnEnemy();
                    context.resetEnemySpawnTimer();
                    context.lastEnemySpawnTime = TimeUtils.nanoTime();
                    Gdx.app.log("EnemySpawn", "Active enemies: " + context.activeEnemies.size + "/" + maxEnemies);
                } else {
                    context.resetEnemySpawnTimer();
                    context.lastEnemySpawnTime = TimeUtils.nanoTime();
                }
            }
        }
        
        // Update enemies with platform collision
        for (int i = context.activeEnemies.size - 1; i >= 0; i--) {
            CommonEnemy enemy = context.activeEnemies.get(i);
            enemy.update(delta, context.platforms, context.grounds);
            
            if (!enemy.isActive()) {
                context.activeEnemies.removeIndex(i);
                context.enemyPool.free(enemy);
            }
        }
        
        // Check bullet-enemy collisions
        checkBulletEnemyCollisions();
        
        // Check bullet-boss collisions
        checkBulletBossCollisions();
        
        // Check enemy bullet-player collisions
        checkEnemyBulletPlayerCollisions();
        
        // Check Level Exit
        checkLevelExit();
        
        // --- CAMERA FOLLOW LOGIC ---
        updateCamera();
        
        // Update Coins
        updateCoins(delta);
        
        // Update Bullets
        updateBullets(delta);
    }
    
    private void spawnEnemy() {
        float cameraLeft = context.camera.position.x - context.viewport.getWorldWidth() / 2;
        float cameraRight = context.camera.position.x + context.viewport.getWorldWidth() / 2;
        
        final float SPAWN_BUFFER = 200f;
        final float PLAYER_SAFETY_ZONE = 300f;
        
        float spawnX;
        int attempts = 0;
        final int MAX_ATTEMPTS = 20;
        
        do {
            boolean spawnLeft = context.random.nextBoolean();
            
            if (spawnLeft) {
                spawnX = cameraLeft - SPAWN_BUFFER - context.random.nextFloat() * 100f;
                if (spawnX < 0) {
                    spawnX = cameraRight + SPAWN_BUFFER + context.random.nextFloat() * 100f;
                }
            } else {
                spawnX = cameraRight + SPAWN_BUFFER + context.random.nextFloat() * 100f;
                if (spawnX > context.currentLevelWidth - 100f) {
                    spawnX = cameraLeft - SPAWN_BUFFER - context.random.nextFloat() * 100f;
                }
            }
            
            attempts++;
            
            if (attempts >= MAX_ATTEMPTS) {
                if (context.player.bounds.x < context.currentLevelWidth / 2) {
                    spawnX = context.currentLevelWidth - 200f;
                } else {
                    spawnX = 100f;
                }
                break;
            }
        } while (Math.abs(spawnX - context.player.bounds.x) < PLAYER_SAFETY_ZONE ||
                 (spawnX >= cameraLeft && spawnX <= cameraRight));
        
        CommonEnemy enemy = context.enemyPool.obtain();
        enemy.init(spawnX, context.player, context.currentLevel);
        context.activeEnemies.add(enemy);
        
        Gdx.app.log("EnemySpawn", "Spawned at X: " + spawnX);
    }
    
    private void checkBulletEnemyCollisions() {
        for (int i = context.activeEnemies.size - 1; i >= 0; i--) {
            CommonEnemy enemy = context.activeEnemies.get(i);
            
            for (int j = context.activeBullets.size - 1; j >= 0; j--) {
                Bullet bullet = context.activeBullets.get(j);
                
                if (enemy.collider.overlaps(bullet.bounds)) {
                    enemy.takeDamage(bullet.damage);
                    context.activeBullets.removeIndex(j);
                    context.bulletPool.free(bullet);
                    Gdx.app.log("Combat", "Enemy hit! Health: " + enemy.health);
                    break;
                }
            }
        }
    }
    
    private void checkBulletBossCollisions() {
        // Mini boss
        if (context.miniBoss != null && !context.miniBoss.isDead()) {
            for (int j = context.activeBullets.size - 1; j >= 0; j--) {
                Bullet bullet = context.activeBullets.get(j);
                
                if (bullet.bounds.overlaps(context.miniBoss.collider)) {
                    context.miniBoss.takeDamage(bullet.damage);
                    context.activeBullets.removeIndex(j);
                    context.bulletPool.free(bullet);
                    Gdx.app.log("Combat", "Mini Boss hit! Health: " + context.miniBoss.health);
                    break;
                }
            }
        }
        
        // Final boss
        if (context.boss != null && !context.boss.isDead()) {
            for (int j = context.activeBullets.size - 1; j >= 0; j--) {
                Bullet bullet = context.activeBullets.get(j);
                
                if (bullet.bounds.overlaps(context.boss.collider)) {
                    context.boss.takeDamage(bullet.damage);
                    context.activeBullets.removeIndex(j);
                    context.bulletPool.free(bullet);
                    Gdx.app.log("Combat", "Boss hit! Health: " + context.boss.health);
                    break;
                }
            }
        }
    }
    
    private void checkEnemyBulletPlayerCollisions() {
        for (int i = context.activeEnemyBullets.size - 1; i >= 0; i--) {
            EnemyBullet eb = context.activeEnemyBullets.get(i);
            
            if (eb.bounds.overlaps(context.player.bounds)) {
                context.player.takeDamage(eb.damage);
                context.activeEnemyBullets.removeIndex(i);
                context.enemyBulletPool.free(eb);
                Gdx.app.log("Combat", "Player hit by enemy bullet!");
            }
        }
    }
    
    private void checkLevelExit() {
        boolean bossDefeated = true;
        if (context.currentLevel == 3 && context.miniBoss != null) {
            bossDefeated = context.miniBoss.isDead();
        }
        if (context.currentLevel == 5 && context.boss != null) {
            bossDefeated = context.boss.isDead();
        }
        
        if (bossDefeated && context.player.bounds.x + context.player.bounds.width >= 
            context.currentLevelWidth - GameContext.LEVEL_EXIT_THRESHOLD) {
            
            if (context.currentLevel == 5) {
                // Level 5 completed - trigger victory!
                if (callback != null) {
                    callback.saveProgress();
                }
                transitionTo(GameState.VICTORY);
                Gdx.app.log("Game", "VICTORY! Game Completed!");
            } else {
                if (callback != null) {
                    callback.saveProgress();
                    callback.loadLevel(context.currentLevel + 1);
                }
            }
        }
    }
    
    private void updateCamera() {
        float halfViewport = context.viewport.getWorldWidth() / 2;
        float levelMid = context.currentLevelWidth / 2;
        
        if (context.viewport.getWorldWidth() >= context.currentLevelWidth) {
            context.camera.position.x = levelMid;
        } else {
            float targetX = context.player.bounds.x + context.player.bounds.width / 2;
            context.camera.position.x = MathUtils.clamp(targetX, halfViewport, 
                                                         context.currentLevelWidth - halfViewport);
        }
        
        context.camera.update();
    }
    
    private void updateCoins(float delta) {
        for (int i = context.activeCoins.size - 1; i >= 0; i--) {
            Coin coin = context.activeCoins.get(i);
            coin.update(delta);
            
            if (coin.isColliding(context.player.bounds)) {
                coin.active = false;
                context.coinScore++;
                context.coinsCollectedThisSession++;
                context.activeCoins.removeIndex(i);
                context.coinPool.free(coin);
                Gdx.app.log("Coin", "Collected! Total: " + context.coinScore);
            }
        }
    }
    
    private void updateBullets(float delta) {
        for (int i = context.activeBullets.size - 1; i >= 0; i--) {
            Bullet b = context.activeBullets.get(i);
            b.update(delta);
            
            boolean shouldRemove = false;
            
            // Check collision with platforms
            for (Platform p : context.platforms) {
                if (b.bounds.overlaps(p.bounds)) {
                    shouldRemove = true;
                    break;
                }
            }
            
            // Check if bullet traveled too far vertically
            if (!shouldRemove && b.isOutOfVerticalBounds(context.viewport.getWorldHeight())) {
                shouldRemove = true;
            }
            
            if (shouldRemove) {
                context.activeBullets.removeIndex(i);
                context.bulletPool.free(b);
            }
        }
    }
    
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        context.batch.setProjectionMatrix(context.camera.combined);
        context.batch.begin();
        
        // Draw background with dynamic viewport dimensions
        // Uses BackgroundRenderer with FIT_HEIGHT strategy for proper scaling at any resolution
        if (context.backgroundRenderer != null) {
            context.backgroundRenderer.render(
                context.batch, 
                context.camera,
                context.viewport.getWorldWidth(),   // Dynamic: adapts to window size
                context.viewport.getWorldHeight(),  // Dynamic: adapts to window size
                context.currentLevelWidth
            );
        }
        
        // Draw grounds
        for (Ground g : context.grounds) {
            g.draw(context.batch);
        }
        
        // Draw platforms
        for (Platform p : context.platforms) {
            p.draw(context.batch);
        }
        
        // Draw exit door (conditional for boss levels)
        boolean bossDefeated = true;
        if (context.currentLevel == 3 && context.miniBoss != null) {
            bossDefeated = context.miniBoss.isDead();
        }
        if (context.currentLevel == 5 && context.boss != null) {
            bossDefeated = context.boss.isDead();
        }
        
        if (bossDefeated) {
            context.batch.draw(context.exitTex, context.currentLevelWidth - 80, 100, 45, 150);
        }
        
        // Draw enemies
        for (CommonEnemy enemy : context.activeEnemies) {
            enemy.draw(context.batch);
        }
        
        // Draw bullets
        for (Bullet b : context.activeBullets) {
            context.batch.draw(context.bulletTex, b.bounds.x, b.bounds.y);
        }
        
        // Draw player
        context.player.draw(context.batch);
        
        // Draw bosses
        if (context.miniBoss != null && !context.miniBoss.isDead()) {
            context.miniBoss.draw(context.batch);
        }
        if (context.boss != null && !context.boss.isDead()) {
            context.boss.draw(context.batch);
        }
        
        // Draw enemy bullets
        for (EnemyBullet eb : context.activeEnemyBullets) {
            eb.draw(context.batch);
        }
        
        context.batch.end();
        
        // RENDER COINS WITH SHAPERENDERER
        context.shapeRenderer.setProjectionMatrix(context.camera.combined);
        context.shapeRenderer.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled);
        for (Coin coin : context.activeCoins) {
            coin.renderShape(context.shapeRenderer);
        }
        context.shapeRenderer.end();
        
        // RENDER UI
        context.batch.begin();
        renderUI();
        context.batch.end();
    }
    
    private void renderUI() {
        // Debug markers
        if (context.currentLevel == 5) {
            float leftEdge = context.camera.position.x - context.viewport.getWorldWidth() / 2;
            float rightEdge = context.camera.position.x + context.viewport.getWorldWidth() / 2 - 10;
            context.batch.draw(context.debugTex, leftEdge, 0);
            context.batch.draw(context.debugTex, rightEdge, 0);
        } else {
            context.batch.draw(context.debugTex, 0, 0);
            context.batch.draw(context.debugTex, context.currentLevelWidth - 10, 0);
        }
        
        // Level indicator
        float levelIndicatorStartX = context.camera.position.x - context.viewport.getWorldWidth() / 2 + 20;
        float levelIndicatorY = context.camera.position.y + context.viewport.getWorldHeight() / 2 - 50;
        for (int i = 0; i < context.currentLevel; i++) {
            context.batch.draw(context.levelIndicatorTex, levelIndicatorStartX + (i * 35), levelIndicatorY);
        }
        
        // Draw Health Bar
        String healthText = "HP: " + (int) context.player.health + "/" + (int) Player.MAX_HEALTH;
        float healthX = context.camera.position.x - context.viewport.getWorldWidth() / 2 + 20;
        float healthY = context.camera.position.y + context.viewport.getWorldHeight() / 2 - 80;
        context.smallFont.draw(context.batch, healthText, healthX, healthY);
        
        // Draw Coin Score
        String coinText = "Coins: " + context.coinScore;
        float coinX = context.camera.position.x - context.viewport.getWorldWidth() / 2 + 20;
        float coinY = healthY - 30;
        context.smallFont.draw(context.batch, coinText, coinX, coinY);
    }
    
    /**
     * Render game without update (for pause/overlay screens).
     */
    public void renderGameOnly() {
        render(0);
    }
}
