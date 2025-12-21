package com.labubushooter.frontend.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.MathUtils;
import com.labubushooter.frontend.GameState;
import com.labubushooter.frontend.core.GameContext;
import com.labubushooter.frontend.objects.Bullet;
import com.labubushooter.frontend.objects.Coin;
import com.labubushooter.frontend.objects.CommonEnemy;
import com.labubushooter.frontend.objects.EnemyBullet;
import com.labubushooter.frontend.objects.Ground;
import com.labubushooter.frontend.objects.Pickup;
import com.labubushooter.frontend.objects.Platform;
import com.labubushooter.frontend.objects.Player;
import com.labubushooter.frontend.patterns.ShootingStrategy;
import com.labubushooter.frontend.patterns.weapons.UnarmedStrategy;

/**
 * Main gameplay screen.
 * Acts as orchestrator delegating to GameWorld for entity management.
 *
 * SOLID Principles Applied:
 * - Single Responsibility: Handles input/rendering, delegates update logic to GameWorld
 * - Open/Closed: Extensible through GameWorld system
 * - Dependency Inversion: Depends on GameWorld abstraction
 *
 * Design Pattern: Facade Pattern
 * - Provides simplified interface to complex game loop subsystems
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
            if (context.currentLevel >= 4) {
                context.player.setWeapon(context.mac10Strategy);
                Gdx.app.log("WeaponSystem", "Mac-10 Equipped");
            } else {
                Gdx.app.log("WeaponSystem", "Mac-10 Locked (Level 4+)");
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
            if (context.unarmedStrategy == null) {
                context.unarmedStrategy = new UnarmedStrategy();
            }
            context.player.setWeapon(context.unarmedStrategy);
            Gdx.app.log("WeaponSystem", "Unarmed");
        }

        // --- RELOAD ---
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            ShootingStrategy weapon = context.player.getWeapon();
            if (weapon != null) {
                weapon.reload();
            }
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
                    context.player.shoot(context.activeBullets, context.bulletPool, context.bulletTex);
                }
            } else {
                if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                    context.player.shoot(context.activeBullets, context.bulletPool, context.bulletTex);
                }
            }
        }
    }

    @Override
    public void update(float delta) {
        // Delegate all entity updates to GameWorld
        if (context.gameWorld != null) {
            context.gameWorld.update(delta);
        }

        // Check Level Exit
        checkLevelExit();

        // --- CAMERA FOLLOW LOGIC ---
        updateCamera();
    }

    private void checkLevelExit() {
        if (context.gameWorld != null && context.gameWorld.isAtLevelExit()) {
            if (context.gameWorld.isFinalLevel()) {
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
            b.draw(context.batch, context.bulletTex);
        }

        // Draw pickups
        if (context.activePickups != null) {
            for (Pickup p : context.activePickups) {
                p.draw(context.batch);
            }
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

        // Draw Ammo Info
        ShootingStrategy weapon = context.player.getWeapon();
        if (weapon != null) {
            String ammoText;
            if (weapon.getName().equals("Unarmed")) {
                ammoText = "Unarmed";
            } else {
                ammoText = weapon.getName() + ": " + weapon.getCurrentMag() + "/" + weapon.getMaxMag() +
                           " (" + weapon.getTotalAmmo() + ")";
                if (weapon.isReloading()) {
                    ammoText += " RELOADING...";
                }
            }
            float ammoX = context.camera.position.x - context.viewport.getWorldWidth() / 2 + 20;
            float ammoY = coinY - 30;
            context.smallFont.draw(context.batch, ammoText, ammoX, ammoY);
        }

        // Draw Mac10 Unlock Message
        if (context.showMac10UnlockMessage) {
            String unlockText = "Mac 10 Acquired! (in level 4) Press 2 to Equip";
            GlyphLayout layout = new GlyphLayout(context.font, unlockText);
            float textX = context.camera.position.x - layout.width / 2;
            float textY = context.camera.position.y + layout.height / 2;
            context.font.draw(context.batch, unlockText, textX, textY);
        }
    }

    /**
     * Render game without update (for pause/overlay screens).
     */
    public void renderGameOnly() {
        render(0);
    }
}
