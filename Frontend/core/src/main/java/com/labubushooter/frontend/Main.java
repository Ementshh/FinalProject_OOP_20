package com.labubushooter.frontend;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.TimeUtils;

// Import design pattern components
import com.labubushooter.frontend.managers.GameManager;
import com.labubushooter.frontend.observers.GameStateObserver;
import com.labubushooter.frontend.factories.EntityFactory;
import com.labubushooter.frontend.factories.PoolFactory;
import com.labubushooter.frontend.commands.*;
import com.labubushooter.frontend.states.*;

// Import existing classes
import com.labubushooter.frontend.objects.*;
import com.labubushooter.frontend.patterns.CoinPattern;
import com.labubushooter.frontend.patterns.LevelStrategy;
import com.labubushooter.frontend.patterns.ShootingStrategy;
import com.labubushooter.frontend.patterns.levels.*;
import com.labubushooter.frontend.patterns.coins.LinePattern;
import com.labubushooter.frontend.patterns.weapons.Mac10Strategy;
import com.labubushooter.frontend.patterns.weapons.PistolStrategy;
import com.labubushooter.frontend.services.PlayerApiService;
import com.labubushooter.frontend.services.PlayerApiService.PlayerData;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Main extends ApplicationAdapter implements GameStateObserver {
    // Managers & Factories (Singleton)
    private GameManager gameManager;
    private EntityFactory entityFactory;
    private PoolFactory poolFactory;
    private CommandInvoker commandInvoker;
    private StateContext stateContext;

    // Rendering
    SpriteBatch batch;
    ShapeRenderer shapeRenderer;
    OrthographicCamera camera;
    Viewport viewport;

    static final float VIEWPORT_WIDTH = 1066f;
    static final float VIEWPORT_HEIGHT = 600f;

    // Object Pools
    private Pool<Bullet> bulletPool;
    private Pool<EnemyBullet> enemyBulletPool;
    private Pool<CommonEnemy> enemyPool;
    private Pool<Coin> coinPool;

    // Active entities
    private Array<Bullet> activeBullets;
    private Array<EnemyBullet> activeEnemyBullets;
    private Array<CommonEnemy> activeEnemies;
    private Array<Coin> activeCoins;

    // Player
    private Player player;

    // Level data
    private int currentLevel = 1;
    private float currentLevelWidth;
    private Map<Integer, LevelStrategy> levelStrategy;
    Array<Platform> platforms;
    Array<Ground> grounds;

    // Textures
    Texture playerTex, platformTex, groundTex, bulletTex, exitTex;
    Texture pistolTex, mac10Tex;
    Texture debugTex;
    Texture levelIndicatorTex;
    Texture enemyTex;
    Texture miniBossTex, bossTex, enemyBulletTex;
    Texture whiteFlashTex, redFlashTex, yellowFlashTex;
    Texture backgroundTex;

    // Bosses
    private MiniBossEnemy miniBoss;
    private FinalBoss boss;

    // Weapons (Strategy Pattern)
    PistolStrategy pistolStrategy;
    Mac10Strategy mac10Strategy;

    // UI
    private BitmapFont font;
    private BitmapFont smallFont;
    private GlyphLayout layout;

    // Backend
    private PlayerApiService playerApi;
    private PlayerData currentPlayerData;
    private String username = "";
    private StringBuilder usernameInput;
    private int coinsCollectedThisSession = 0;
    private boolean isNewPlayer = false;

    // Game state - delegated to GameManager
    private GameState gameState = GameState.USERNAME_INPUT;

    // Other fields
    private DebugManager debugManager;
    private Random random;

    // ... existing fields ...

    @Override
    public void create() {
        // Initialize singletons
        gameManager = GameManager.getInstance();
        entityFactory = EntityFactory.getInstance();
        poolFactory = PoolFactory.getInstance();
        commandInvoker = CommandInvoker.getInstance();

        // Register as observer
        gameManager.addObserver(this);

        // Initialize rendering
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        camera = new OrthographicCamera();
        viewport = new FitViewport(VIEWPORT_WIDTH, VIEWPORT_HEIGHT, camera);
        camera.position.set(VIEWPORT_WIDTH / 2, VIEWPORT_HEIGHT / 2, 0);

        // Load textures
        playerTex = new Texture(Gdx.files.internal("player.png"));
        playerTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        platformTex = new Texture(Gdx.files.internal("ground.png"));
        platformTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        groundTex = new Texture(Gdx.files.internal("ground_base.png"));
        groundTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        bulletTex = createColorTexture(10, 5, Color.YELLOW);
        pistolTex = createColorTexture(20, 10, Color.GRAY);
        mac10Tex = createColorTexture(30, 15, Color.LIME);
        exitTex = new Texture(Gdx.files.internal("door.png"));
        exitTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        debugTex = createColorTexture(10, 600, Color.RED);

        // Boss textures
        enemyTex = new Texture(Gdx.files.internal("enemy.png"));
        miniBossTex = new Texture(Gdx.files.internal("miniboss.png"));
        bossTex = new Texture(Gdx.files.internal("boss.png"));
        enemyBulletTex = createColorTexture(8, 8, Color.RED);
        whiteFlashTex = createColorTexture(60, 90, Color.WHITE);
        redFlashTex = createColorTexture(60, 100, Color.RED);
        yellowFlashTex = createColorTexture(60, 90, Color.YELLOW);

        // Initialize EntityFactory with textures
        entityFactory.initialize(playerTex, enemyTex, miniBossTex, bossTex,
                bulletTex, enemyBulletTex, whiteFlashTex,
                redFlashTex, yellowFlashTex);

        // Create pools using PoolFactory
        bulletPool = poolFactory.createBulletPool();
        enemyBulletPool = poolFactory.createEnemyBulletPool();
        enemyPool = poolFactory.createEnemyPool();
        coinPool = poolFactory.createCoinPool();

        // Initialize active arrays
        activeBullets = new Array<>();
        activeEnemyBullets = new Array<>();
        activeEnemies = new Array<>();
        activeCoins = new Array<>();

        // Create player using factory
        player = entityFactory.createPlayer();
        player.camera = camera;
        player.pistolTex = pistolTex;
        player.mac10Tex = mac10Tex;

        // Initialize weapons
        pistolStrategy = new PistolStrategy();
        mac10Strategy = new Mac10Strategy();

        // Initialize fonts
        font = new BitmapFont();
        font.getData().setScale(2f);
        smallFont = new BitmapFont();
        smallFont.getData().setScale(1.5f);
        layout = new GlyphLayout();

        // Initialize level strategies
        levelStrategy = new HashMap<>();
        levelStrategy.put(1, new Level1Strategy());
        levelStrategy.put(2, new Level2Strategy());
        levelStrategy.put(3, new Level3Strategy());
        levelStrategy.put(4, new Level4Strategy());
        levelStrategy.put(5, new Level5Strategy());

        platforms = new Array<>();
        grounds = new Array<>();

        // Initialize backend service
        playerApi = new PlayerApiService();
        usernameInput = new StringBuilder();

        // Initialize debug manager
        debugManager = new DebugManager();
        random = new Random();

        // Initialize state handlers
        stateContext = new StateContext();
        stateContext.registerState(GameState.PLAYING, new PlayingState(this));
        stateContext.registerState(GameState.PAUSED, new PausedState(this));
        stateContext.registerState(GameState.USERNAME_INPUT, new UsernameInputState(this));
        stateContext.registerState(GameState.GAME_OVER, new GameOverState(this));

        // Set initial state
        gameManager.setState(GameState.USERNAME_INPUT);
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();

        // Clear screen
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Debug mode check
        if (debugManager.checkDebugActivation()) {
            username = debugManager.getDebugUsername();
            currentPlayerData = debugManager.createDebugPlayerData();
            coinsCollectedThisSession = 0;
            isNewPlayer = true;
            gameManager.setState(GameState.PLAYING);
            loadLevel(debugManager.getDebugStartingLevel());
            return;
        }

        // Delegate to state handler
        stateContext.handleInput();
        stateContext.update(delta);

        batch.begin();
        stateContext.render(batch);
        batch.end();
    }

    @Override
    public void onStateChanged(GameState oldState, GameState newState) {
        Gdx.app.log("StateChange", "State changed from " + oldState + " to " + newState);
        stateContext.changeState(newState);
        gameState = newState; // Keep for compatibility
    }

    // Public methods for state handlers
    public void pauseGame() {
        gameManager.setState(GameState.PAUSED);
    }

    public void resumeGame() {
        gameManager.setState(GameState.PLAYING);
    }

    public void updateGameplay(float delta) {
        // Your existing gameplay update logic
        player.update(delta, platforms, grounds);

        // Update bullets
        for (int i = activeBullets.size - 1; i >= 0; i--) {
            Bullet b = activeBullets.get(i);
            b.update(delta);

            if (b.isOutOfVerticalBounds(600)) {
                activeBullets.removeIndex(i);
                bulletPool.free(b);
            }
        }

        // Update enemies
        for (CommonEnemy enemy : activeEnemies) {
            enemy.update(delta, platforms, grounds);
        }

        // Update bosses
        if (miniBoss != null && !miniBoss.isDead()) {
            miniBoss.update(delta, platforms, grounds, player);
        }

        if (boss != null && !boss.isDead()) {
            boss.update(delta, platforms, grounds, player, activeEnemyBullets, enemyBulletPool);
        }

        // ... rest of your update logic ...
    }

    public void renderGameplay(SpriteBatch batch) {
        // Your existing rendering logic
        player.draw(batch);

        for (Bullet b : activeBullets) {
            // render bullets
        }

        for (CommonEnemy enemy : activeEnemies) {
            enemy.draw(batch);
        }

        if (miniBoss != null) {
            miniBoss.draw(batch);
        }

        if (boss != null) {
            boss.draw(batch);
        }

        // ... rest of your rendering ...
    }

    public void handlePauseMenuInput() {
        // Your existing pause menu input handling
        if (Gdx.input.justTouched()) {
            // Handle button clicks
            Gdx.app.log("PauseMenu", "Touch detected in pause menu");
        }
    }

    public void renderPauseMenu(SpriteBatch batch) {
        // Your existing pause menu rendering
        batch.setProjectionMatrix(camera.combined);

        String pauseText = "PAUSED";
        layout.setText(font, pauseText);
        float pauseX = camera.position.x - layout.width / 2;
        float pauseY = camera.position.y + 50;
        font.draw(batch, pauseText, pauseX, pauseY);

        String resumeText = "Press ESC to resume";
        layout.setText(smallFont, resumeText);
        float resumeX = camera.position.x - layout.width / 2;
        float resumeY = camera.position.y - 20;
        smallFont.draw(batch, resumeText, resumeX, resumeY);
    }

    public void handleUsernameInput() {
        // Character input
        for (int i = 0; i < 26; i++) {
            int key = Input.Keys.A + i;
            if (Gdx.input.isKeyJustPressed(key)) {
                if (usernameInput.length() < 20) {
                    char c = (char) ('a' + i);
                    if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) ||
                            Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
                        c = Character.toUpperCase(c);
                    }
                    usernameInput.append(c);
                }
            }
        }

        // Number input
        for (int i = 0; i <= 9; i++) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_0 + i)) {
                if (usernameInput.length() < 20) {
                    usernameInput.append(i);
                }
            }
        }

        // Backspace
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) {
            if (usernameInput.length() > 0) {
                usernameInput.deleteCharAt(usernameInput.length() - 1);
            }
        }

        // Submit with Enter
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            username = usernameInput.toString().trim();

            if (!username.isEmpty()) {
                gameManager.setState(GameState.PLAYING);
                loadLevel(1);
            }
        }
    }

    public void renderUsernameInput(SpriteBatch batch) {
        batch.setProjectionMatrix(camera.combined);

        String titleText = "Enter Username";
        layout.setText(font, titleText);
        float titleX = VIEWPORT_WIDTH / 2 - layout.width / 2;
        float titleY = 450;
        font.draw(batch, titleText, titleX, titleY);

        String displayText = usernameInput.length() > 0 ? usernameInput.toString() : "Username...";
        layout.setText(smallFont, displayText);
        float textX = VIEWPORT_WIDTH / 2 - layout.width / 2;
        float textY = 300;

        if (usernameInput.length() > 0) {
            smallFont.draw(batch, displayText, textX, textY);
        } else {
            smallFont.setColor(0.5f, 0.5f, 0.5f, 1f);
            smallFont.draw(batch, displayText, textX, textY);
            smallFont.setColor(1, 1, 1, 1);
        }

        String instructionText = "Press ENTER to submit";
        layout.setText(smallFont, instructionText);
        float instrX = VIEWPORT_WIDTH / 2 - layout.width / 2;
        smallFont.draw(batch, instructionText, instrX, 150);
    }

    public void renderGameOver(SpriteBatch batch) {
        batch.setProjectionMatrix(camera.combined);

        String gameOverText = "GAME OVER";
        layout.setText(font, gameOverText);
        float gameOverX = camera.position.x - layout.width / 2;
        float gameOverY = camera.position.y + 50;
        font.draw(batch, gameOverText, gameOverX, gameOverY);

        String restartText = "Press SPACE to restart";
        layout.setText(smallFont, restartText);
        float restartX = camera.position.x - layout.width / 2;
        float restartY = camera.position.y - 20;
        smallFont.draw(batch, restartText, restartX, restartY);
    }

    public void restartGame() {
        gameManager.setState(GameState.PLAYING);
        currentLevel = 1;

        for (CommonEnemy enemy : activeEnemies) {
            enemyPool.free(enemy);
        }
        activeEnemies.clear();

        activeBullets.clear();

        for (Coin coin : activeCoins) {
            coinPool.free(coin);
        }
        activeCoins.clear();

        player.reset();
        player.setWeapon(null);

        loadLevel(1);
        Gdx.app.log("Game", "Game Restarted");
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public void loadLevel(int level) {
        // Use command pattern for level loading
        LoadLevelCommand loadCommand = new LoadLevelCommand(this, level);
        commandInvoker.executeCommand(loadCommand);
    }

    // Internal loadLevel implementation
    void loadLevelInternal(int level) {
        // Your existing loadLevel logic
        LevelStrategy strategy = levelStrategy.get(level);

        if (strategy == null) {
            System.out.println("Level " + level + " not found!");
            return;
        }

        this.currentLevel = level;

        if (platforms == null) {
            platforms = new Array<Platform>();
        } else {
            platforms.clear();
        }
        if (grounds == null) {
            grounds = new Array<Ground>();
        } else {
            grounds.clear();
        }

        // Clear pools
        for (CommonEnemy enemy : activeEnemies) {
            enemyPool.free(enemy);
        }
        activeEnemies.clear();

        for (Coin coin : activeCoins) {
            coinPool.free(coin);
        }
        activeCoins.clear();

        activeBullets.clear();
        activeEnemyBullets.clear();

        // Load level using strategy
        strategy.loadPlatforms(platforms, platformTex);
        strategy.loadGround(grounds, groundTex);
        currentLevelWidth = strategy.getLevelWidth();

        // Reset player position
        player.bounds.x = strategy.getPlayerStartX();
        player.bounds.y = strategy.getPlayerStartY();
        player.velY = 0;
        player.grounded = false;

        // Create bosses using factory
        if (level == 3) {
            miniBoss = entityFactory.createMiniBoss();
            miniBoss.init(strategy.getBossSpawnX(), strategy.getBossSpawnY());
        } else {
            miniBoss = null;
        }

        if (level == 5) {
            boss = entityFactory.createFinalBoss();
            boss.init(strategy.getBossSpawnX(), strategy.getBossSpawnY());
        } else {
            boss = null;
        }

        Gdx.app.log("LevelSystem", "Loaded Level " + level);
    }

    public void saveProgress() {
        if (debugManager.isDebugModeActive()) {
            debugManager.logSkippedAction("Save progress to backend");
            return;
        }

        // Use command pattern for saving
        SaveGameCommand saveCommand = new SaveGameCommand(
                playerApi,
                currentPlayerData,
                currentLevel,
                coinsCollectedThisSession);
        commandInvoker.executeCommand(saveCommand);
    }

    private Texture createColorTexture(int width, int height, Color color) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        playerTex.dispose();
        platformTex.dispose();
        groundTex.dispose();
        // ... dispose other resources ...
    }
}