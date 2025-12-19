package com.labubushooter.frontend.factories;

import com.badlogic.gdx.graphics.Texture;
import com.labubushooter.frontend.objects.*;

public class EntityFactory {
    private static EntityFactory instance;

    private Texture playerTex;
    private Texture enemyTex;
    private Texture miniBossTex;
    private Texture bossTex;
    private Texture bulletTex;
    private Texture enemyBulletTex;
    private Texture whiteFlashTex;
    private Texture redFlashTex;
    private Texture yellowFlashTex;

    private EntityFactory() {}

    public static EntityFactory getInstance() {
        if (instance == null) {
            instance = new EntityFactory();
        }
        return instance;
    }

    public void initialize(Texture playerTex, Texture enemyTex, Texture miniBossTex,
                           Texture bossTex, Texture bulletTex, Texture enemyBulletTex,
                           Texture whiteFlashTex, Texture redFlashTex, Texture yellowFlashTex) {
        this.playerTex = playerTex;
        this.enemyTex = enemyTex;
        this.miniBossTex = miniBossTex;
        this.bossTex = bossTex;
        this.bulletTex = bulletTex;
        this.enemyBulletTex = enemyBulletTex;
        this.whiteFlashTex = whiteFlashTex;
        this.redFlashTex = redFlashTex;
        this.yellowFlashTex = yellowFlashTex;
    }

    public Player createPlayer() {
        return new Player(playerTex);
    }

    public CommonEnemy createEnemy() {
        return new CommonEnemy(enemyTex);
    }

    public MiniBossEnemy createMiniBoss() {
        return new MiniBossEnemy(miniBossTex, whiteFlashTex, yellowFlashTex);
    }

    public FinalBoss createFinalBoss() {
        return new FinalBoss(bossTex, redFlashTex, enemyBulletTex, yellowFlashTex);
    }

    public Bullet createBullet() {
        return new Bullet();
    }

    public EnemyBullet createEnemyBullet() {
        return new EnemyBullet();
    }

    public Coin createCoin() {
        return new Coin();
    }
}