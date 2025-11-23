package org.newdawn.spaceinvaders;

import org.newdawn.spaceinvaders.entity.BossEntity;
import org.newdawn.spaceinvaders.entity.Entity;

import java.util.Random;

public interface GameContext {

    long getAlienFireIntervalMs();
    long getAsteroidIntervalMs();
    long getBlackHoleIntervalMs();

    boolean fireRandomAlienShot();
    void spawnAsteroidRandom(Random rng);
    void spawnBlackHoleAroundPlayer(Random rng);

    void addEntity(Entity e);
    void removeEntity(Entity e);

    void onAlienKilledAt(int cx, int cy);
    void onBossDefeated(BossEntity boss);
    void onPlayerHit();
    void activateBombAt(int cx, int cy);
    boolean isPlayerInvincible();

    boolean collectBomb();
    boolean collectLaser();

    int getShipCenterX();
    int getVirtualWidth();
    int getVirtualHeight();

    /** Alien이 화면 끝에 닿았을 때 "이번 루프에 doLogic() 좀 돌려줘" 요청 */
    void requestLogicUpdate();

    void tickLaserAt(int cx, int halfWidth);
}
