package com.labubushooter.frontend.factories;

import com.badlogic.gdx.utils.Pool;
import com.labubushooter.frontend.objects.*;

public class PoolFactory {
    private static PoolFactory instance;
    private EntityFactory entityFactory;

    private PoolFactory() {
        entityFactory = EntityFactory.getInstance();
    }

    public static PoolFactory getInstance() {
        if (instance == null) {
            instance = new PoolFactory();
        }
        return instance;
    }

    public Pool<Bullet> createBulletPool() {
        return new Pool<Bullet>() {
            @Override
            protected Bullet newObject() {
                return entityFactory.createBullet();
            }
        };
    }

    public Pool<EnemyBullet> createEnemyBulletPool() {
        return new Pool<EnemyBullet>() {
            @Override
            protected EnemyBullet newObject() {
                return entityFactory.createEnemyBullet();
            }
        };
    }

    public Pool<CommonEnemy> createEnemyPool() {
        return new Pool<CommonEnemy>() {
            @Override
            protected CommonEnemy newObject() {
                return entityFactory.createEnemy();
            }
        };
    }

    public Pool<Coin> createCoinPool() {
        return new Pool<Coin>() {
            @Override
            protected Coin newObject() {
                return entityFactory.createCoin();
            }
        };
    }
}