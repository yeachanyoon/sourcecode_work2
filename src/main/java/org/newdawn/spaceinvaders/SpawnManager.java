package org.newdawn.spaceinvaders;

import java.util.Random;

/**
 * 스폰/발사 타이밍 담당 매니저
 * - 적(외계인) 총알 발사 타이밍
 * - 유성 스폰 타이밍
 * - 블랙홀 스폰 타이밍
 *
 * 실제 "무엇을 어떻게 스폰"하는지는 GameContext에 위임한다.
 */
public class SpawnManager {

    private final GameContext ctx;
    private final Random rng = new Random();

    /** 마지막 외계인 사격 시각(ms) */
    private long lastAlienFire = 0;

    /** 다음 유성 스폰 시각(ms) */
    private long nextAsteroidSpawn = 0;

    /** 다음 블랙홀 스폰 시각(ms) */
    private long nextBlackHoleSpawn = 0;

    public SpawnManager(GameContext ctx) {
        this.ctx = ctx;
    }

    /**
     * 한 프레임당 한 번 호출해서,
     * - 외계인 사격
     * - 유성/블랙홀 스폰
     * 타이밍을 모두 처리한다.
     */
    public void update(long nowMillis) {
        tryAlienFire(nowMillis);
        spawnAsteroidIfNeeded(nowMillis);
        spawnBlackHoleIfNeeded(nowMillis);
    }

    /** 외계인 사격 타이밍 처리 */
    private void tryAlienFire(long now) {
        long interval = ctx.getAlienFireIntervalMs();  // GameContext에 getter 선언
        if (now - lastAlienFire < interval) {
            return;
        }

        // 실제 발사: 어떤 외계인이 쏠지, 총알 생성은 GameContext에 위임
        if (ctx.fireRandomAlienShot()) {
            lastAlienFire = now;
        }
    }

    /** 유성 스폰 타이밍 처리 */
    private void spawnAsteroidIfNeeded(long now) {
        long interval = ctx.getAsteroidIntervalMs();
        if (interval <= 0) return;

        if (nextAsteroidSpawn == 0L) {
            nextAsteroidSpawn = now + interval;
            return;
        }

        if (now >= nextAsteroidSpawn) {
            ctx.spawnAsteroidRandom(rng);
            nextAsteroidSpawn = now + interval;
        }
    }

    /** 블랙홀 스폰 타이밍 처리 */
    private void spawnBlackHoleIfNeeded(long now) {
        long interval = ctx.getBlackHoleIntervalMs();
        if (interval <= 0) return;

        if (nextBlackHoleSpawn == 0L) {
            nextBlackHoleSpawn = now + interval;
            return;
        }

        if (now >= nextBlackHoleSpawn) {
            ctx.spawnBlackHoleAroundPlayer(rng);
            nextBlackHoleSpawn = now + interval;
        }
    }

    /** (선택) 리셋할 때 호출용 */
    public void resetTimers() {
        lastAlienFire = 0L;
        nextAsteroidSpawn = 0L;
        nextBlackHoleSpawn = 0L;
    }
}
