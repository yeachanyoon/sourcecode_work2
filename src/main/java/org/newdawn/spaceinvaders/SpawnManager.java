package org.newdawn.spaceinvaders;

import org.newdawn.spaceinvaders.entity.*;

import java.util.List;
import java.util.Random;

public class SpawnManager {
    private final GameContext ctx;  // Game이 구현
    private final Game game;        // 실제 구현체 (편의를 위해 캐스팅)
    private final Random rng = new Random();

    /* ===== 타이머/간격 ===== */
    private long lastAlienFire   = 0;
    private long alienFireIntervalMs = 1200;   // 에일리언 탄 발사 간격

    private long nextAsteroidSpawn = 0;
    private long asteroidIntervalMs = 3000;    // 유성 스폰 간격

    private long nextBlackHoleSpawn = 0;
    private long blackHoleIntervalMs = 15000;  // 블랙홀 스폰 간격

    /* ===== 블랙홀 파라미터 ===== */
    private double blackHoleRadius = 180.0;
    private long   blackHoleLifeMs = 7000;

    /* ===== 보스 ===== */
    private boolean bossSpawned = false;
    private BossEntity bossRef  = null;

    public SpawnManager(GameContext ctx) {
        this.ctx = ctx;
        this.game = (Game) ctx; // Game이 GameContext를 구현하므로 안전한 캐스팅
    }

    /* === 레벨별로 간격 튜닝할 수 있도록 세터 제공(원하면 Game.applyLevelParams에서 호출) === */
    public void setAlienFireIntervalMs(long ms)   { this.alienFireIntervalMs = ms; }
    public void setAsteroidIntervalMs(long ms)    { this.asteroidIntervalMs = ms; }
    public void setBlackHoleIntervalMs(long ms)   { this.blackHoleIntervalMs = ms; }
    public void setBlackHoleParams(double radius, long lifeMs) {
        this.blackHoleRadius = radius;
        this.blackHoleLifeMs = lifeMs;
    }

    /** 새 게임 시작 시 초기화용(원하면 Game.startGame에서 호출) */
    public void resetForNewRun(long nowMs) {
        lastAlienFire      = nowMs;
        nextAsteroidSpawn  = nowMs + asteroidIntervalMs;
        nextBlackHoleSpawn = nowMs + blackHoleIntervalMs;
        bossSpawned        = false;
        bossRef            = null;
    }

    /* ===================================================================
       1) 에일리언 탄 발사
       - nowMs: SystemTimer.getTime() 값
       - 일정 간격(alienFireIntervalMs)마다 랜덤 에일리언 하나가 아래로 탄을 쏨
       =================================================================== */
    public void tryAlienFire(long nowMs) {
        // 아직 간격이 안 지났으면 패스
        if (nowMs - lastAlienFire < alienFireIntervalMs) {
            return;
        }

        Entity shooter = pickRandomAlien();
        if (shooter == null) {
            return; // 에일리언이 없으면 발사 X
        }

        int sx = shooter.getX() + shooter.getWidth() / 2;
        int sy = shooter.getY() + shooter.getHeight();

        game.addEntity(new AlienShotEntity(game, "sprites/shot.gif", sx, sy));
        lastAlienFire = nowMs;
    }

    /** 현재 필드에서 랜덤 에일리언 한 마리 선택 */
    private Entity pickRandomAlien() {
        List<Entity> entities = game.getEntities();
        // 에일리언만 세어서 배열 대신 인덱스 접근
        int count = 0;
        for (Entity e : entities) {
            if (e instanceof AlienEntity) count++;
        }
        if (count == 0) return null;

        int target = rng.nextInt(count); // [0, count)
        int idx = 0;
        for (Entity e : entities) {
            if (e instanceof AlienEntity) {
                if (idx == target) return e;
                idx++;
            }
        }
        return null; // 이론상 도달 X
    }

    /* ===================================================================
       2) 유성 스폰
       - nowMs: SystemTimer.getTime()
       - nextAsteroidSpawn 시각이 되면 화면 상단 바깥에서 유성이 떨어짐
       =================================================================== */
    public void spawnAsteroidIfNeeded(long nowMs) {
        if (nextAsteroidSpawn == 0) {
            // 최초 호출 시 기준값 설정
            nextAsteroidSpawn = nowMs + asteroidIntervalMs;
            return;
        }
        if (nowMs < nextAsteroidSpawn) {
            return;
        }

        int vw = game.VIRTUAL_WIDTH;   // static 상수라 바로 사용
        int x = rng.nextInt(Math.max(1, vw - 32));
        int y = -32;
        double fallSpeed = 500.0;

        game.addEntity(new AsteroidEntity(game, x, y, fallSpeed));

        nextAsteroidSpawn = nowMs + asteroidIntervalMs;
    }

    /* ===================================================================
       3) 블랙홀 스폰
       - nowMs: SystemTimer.getTime()
       - 일정 간격마다 플레이어 주변(원형 링) 위치에 블랙홀 생성
       =================================================================== */
    public void spawnBlackHoleIfNeeded(long nowMs) {
        if (nextBlackHoleSpawn == 0) {
            nextBlackHoleSpawn = nowMs + blackHoleIntervalMs;
            return;
        }
        if (nowMs < nextBlackHoleSpawn) {
            return;
        }

        int vw = Game.VIRTUAL_WIDTH;
        int vh = Game.VIRTUAL_HEIGHT;

        int w = 48;
        int h = 48;

        // 중심 기준: 배가 있으면 배 중심 X, 없으면 화면 중앙
        double sx = game.getShipCenterX();
        double sy = vh / 2.0; // Y는 대충 화면 중간을 기준으로 (원본과 크게 다르지 않음)

        double minD = 120.0;
        double maxD = 220.0;
        double ang  = rng.nextDouble() * Math.PI * 2.0;
        double dist = minD + rng.nextDouble() * (maxD - minD);

        int x = (int) Math.round(sx + Math.cos(ang) * dist) - w / 2;
        int y = (int) Math.round(sy + Math.sin(ang) * dist) - h / 2;

        // 화면 안으로 클램프
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        if (x > vw - w) x = vw - w;
        if (y > vh - h) y = vh - h;

        game.addEntity(new BlackHoleEntity(game, x, y, blackHoleRadius, 0.60f, blackHoleLifeMs));
        nextBlackHoleSpawn = nowMs + blackHoleIntervalMs;
    }

    /* ===================================================================
       4) 보스 스폰
       - aliveAliens: 현재 남아 있는 에일리언 수 (Game에서 계산해서 넘겨줌)
       - 에일리언이 0이고 아직 보스가 안 나왔으면 스폰
         (보스 처치 후 승리 판정은 Game 쪽에서 별도로 처리)
       =================================================================== */
    public void spawnBossIfNeeded(int aliveAliens) {
        // 에일리언이 남아있으면 보스 스폰 조건이 아님
        if (aliveAliens > 0) return;

        // 이미 스폰했다면 더 이상 아무 것도 하지 않음
        if (bossSpawned) return;

        int bw = 120;
        int bx = (Game.VIRTUAL_WIDTH - bw) / 2;
        int by = 60;

        BossEntity boss = new BossEntity(game, bx, by);
        this.bossRef = boss;
        this.bossSpawned = true;

        game.addEntity(boss);
        // "보스 등장!" 토스트는 Game에서 showToast(...)로 직접 띄워도 되고,
        // 필요하다면 GameContext에 showToast를 노출해서 여기서 호출해도 된다.
    }

    /** Game에서 보스 참조가 필요할 때 사용할 수 있는 게터(옵션) */
    public BossEntity getBoss() {
        return bossRef;
    }

    /** 새 게임 시작 시 보스 관련 플래그만 리셋하고 싶을 때(옵션) */
    public void resetBoss() {
        bossSpawned = false;
        bossRef = null;
    }
}
