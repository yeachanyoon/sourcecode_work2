package org.newdawn.spaceinvaders.screen;

import org.newdawn.spaceinvaders.*;
import org.newdawn.spaceinvaders.entity.*;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.newdawn.spaceinvaders.Game.VIRTUAL_WIDTH;
import static org.newdawn.spaceinvaders.Game.VIRTUAL_HEIGHT;

/**
 * 한 판 “인게임” 상태를 담당하는 화면.
 * - Game은 창/루프/저장/메뉴 전환을 담당
 * - PlayScreen은 엔티티, 이동, 공격, 보스, HUD, 토스트 등 런타임 로직 담당
 */
public class PlayScreen implements Screen, GameContext {

    private final Game game;

    // 인게임 엔티티 / 월드
    private final List<Entity> entities   = new ArrayList<>();
    private final List<Entity> removeList = new ArrayList<>();
    private final World world;
    private final SpawnManager spawnManager;

    // 플레이어
    private Entity ship;
    private final int selectedShipIndex;
    private final int selectedLevel;

    private int lvSpeed = 0, lvFireRate = 0, lvShield = 0, lvBomb = 0, lvLaser = 0;

    // 이동/공격 파라미터
    private double moveSpeed      = 300;
    private long   lastFire       = 0;
    private long   firingInterval = 500;

    // 폭탄
    private long lastBombFire     = 0;
    private long bombFireInterval = 400;
    private int  bombCount        = 0;
    private final int bombMax     = 2;

    // 레이저
    private long lastLaserUse     = 0;
    private long laserCooldown    = 500;
    private int  laserCount       = 0;
    private final int laserMax    = 1;

    // 목숨 / 점수
    private final int maxLives = 3;
    private int       lives    = maxLives;
    private int       score    = 0;

    // 보스
    private BossEntity boss;
    private boolean bossSpawned  = false;
    private boolean bossDefeated = false;

    // 레벨별 파라미터
    private double levelBombDrop      = 0.10;
    private double levelLaserDrop     = 0.06;
    private double levelAlienSpeedMul = 0.80;

    // 블랙홀 효과 (이동 속도 감속용)
    private float blackHoleRadius = 180f;
    private long  blackHoleLifeMs = 7000L;

    private final Random rng = new Random();

    // 토스트 메시지
    private String toastText  = null;
    private long   toastUntil = 0;

    // 런 통계
    private int  totalKills       = 0;
    private int  shotsFiredRun    = 0;
    private long runStartTime     = 0;
    private long lastRunElapsedMs = -1;

    // 도전과제(이번 런 기준)
    private boolean achKill10    = false;
    private boolean achClear100  = false;
    private boolean achClear1Min = false;

    // 기체 #3 방어막 관련
    private boolean shieldActive         = false;
    private long    invulnUntil          = 0;
    private int     killsSinceLastShield = 0;

    // 입력 상태
    private final InputState input = new InputState();

    // 적/유성/블랙홀 스폰 간격 (SpawnManager에서 사용)
    private long alienFireIntervalMs = 1200;   // 기본값: 1.2초
    private long asteroidIntervalMs  = 3000;   // 기본값: 3초
    private long blackHoleIntervalMs = 15000;  // 기본값: 15초


    public PlayScreen(Game game, int level, int shipIndex) {
        this.game = game;
        this.lvLaser = game.getLvLaser();
        this.selectedLevel = level;
        this.selectedShipIndex = shipIndex;

        this.spawnManager = new SpawnManager(this);
        this.world        = new World(this, entities, removeList, spawnManager);

        LevelConfig cfg = LevelConfig.forLevel(level);
        this.levelBombDrop      = cfg.bombDropRate;
        this.levelLaserDrop     = cfg.laserDropRate;
        this.levelAlienSpeedMul = cfg.alienSpeedMultiplier;


        initEntitiesForLevel(level, shipIndex);
        this.runStartTime = SystemTimer.getTime();
    }

    /** 레벨별 파라미터 적용 (필요하면 Game의 기존 로직과 맞춰도 됨) */
    private void applyLevelParams(int level) {
        // 기본값(레벨 1)
        levelBombDrop      = 0.10;
        levelLaserDrop     = 0.06;
        levelAlienSpeedMul = 0.80;

        switch (level) {
            case 2:
                levelBombDrop      = 0.08;
                levelLaserDrop     = 0.05;
                levelAlienSpeedMul = 0.90;
                break;
            case 3:
                levelBombDrop      = 0.07;
                levelLaserDrop     = 0.04;
                levelAlienSpeedMul = 1.00;
                break;
            case 4:
                levelBombDrop      = 0.06;
                levelLaserDrop     = 0.03;
                levelAlienSpeedMul = 1.10;
                break;
            case 5:
                levelBombDrop      = 0.05;
                levelLaserDrop     = 0.02;
                levelAlienSpeedMul = 1.20;
                break;
            default:
                break;
        }
    }

    /** 이 레벨에 맞는 ship과 alien들을 배치 */
    private void initEntitiesForLevel(int level, int shipIndex) {
        // 1) ship 스프라이트 결정
        String shipImage;
        switch (shipIndex) {
            case 1: shipImage = "sprites/ship2.png"; break;
            case 2: shipImage = "sprites/ship3.png"; break;
            case 0:
            default: shipImage = "sprites/ship.gif"; break;
        }

        // ★ Game이 아니라 PlayScreen(this)을 GameContext로 넘겨야 함
        ship = new ShipEntity(this, shipImage, 370, 550);
        entities.add(ship);

        // 2) 에일리언 생성 + 레벨별 속도 배수
        double spMul = levelAlienSpeedMul;

        int rows   = 5;
        int cols   = 12;
        int startX = 100;
        int startY = 50;
        int dx     = 50;
        int dy     = 30;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                AlienEntity alien = new AlienEntity(this, startX + c * dx, startY + r * dy);
                alien.setHorizontalMovement(alien.getHorizontalMovement() * spMul);
                entities.add(alien);
            }
        }
    }

    // ========================================================
    // GameContext 구현부
    // ========================================================

    @Override
    public long getAlienFireIntervalMs() {
        return alienFireIntervalMs;
    }

    @Override
    public long getAsteroidIntervalMs() {
        return asteroidIntervalMs;
    }

    @Override
    public long getBlackHoleIntervalMs() {
        return blackHoleIntervalMs;
    }

    @Override
    public boolean fireRandomAlienShot() {
        // 현재 화면(PlayScreen)에 살아있는 Alien들 중 랜덤 선택
        List<Entity> aliens = new ArrayList<>();
        for (Entity e : entities) {
            if (e instanceof AlienEntity) {
                aliens.add(e);
            }
        }
        if (aliens.isEmpty()) return false;

        Entity shooter = aliens.get(rng.nextInt(aliens.size()));
        int sx = (int) (shooter.getX() + 12);
        int sy = (int) (shooter.getY() + 20);
        entities.add(new AlienShotEntity(this, "sprites/shot.gif", sx, sy));
        return true;
    }

    @Override
    public void spawnAsteroidRandom(Random rng) {
        if (world != null) {
            world.spawnAsteroidRandom(rng);
        }
    }

    @Override
    public void spawnBlackHoleAroundPlayer(Random rng) {
        if (world != null) {
            world.spawnBlackHoleAroundPlayer(
                    rng,
                    blackHoleRadius,
                    0.60f,
                    blackHoleLifeMs
            );
        }
    }

    @Override
    public void addEntity(Entity e) {
        entities.add(e);
    }

    @Override
    public void removeEntity(Entity e) {
        removeList.add(e);
    }

    @Override
    public void onAlienKilledAt(int cx, int cy) {
        // 점수, 드랍, 도전과제 등 이쪽으로 이사
        totalKills++;

        if (!achKill10 && totalKills >= 10) {
            achKill10 = true;
            showToast("도전과제 달성: 적 10마리 격파!", 2000);
        }

        // 기체 #3 방어막 스택
        if (selectedShipIndex == 2) {
            killsSinceLastShield++;
            if (killsSinceLastShield >= 30 && !shieldActive) {
                shieldActive = true;
                killsSinceLastShield = 0;
                showToast("방어막 획득! (적 30마리 처치)", 2000);
            }
        }

        // 남은 Alien들 약간씩 가속
        for (Entity e : entities) {
            if (e instanceof AlienEntity) {
                e.setHorizontalMovement(e.getHorizontalMovement() * 1.02);
            }
        }

        // 점수/코인 (간단히 점수만 올리고 싶다면 이렇게)
        addScore(100);

        // 드랍(폭탄/레이저)
        if (cx >= 0) {
            if (rng.nextDouble() < levelBombDrop) {
                int spriteHalf = 12;
                spawnBombItemAt(cx - spriteHalf, cy - spriteHalf);
            }
            if (rng.nextDouble() < levelLaserDrop) {
                int spriteHalf = 12;
                spawnLaserItemAt(cx - spriteHalf, cy - spriteHalf);
            }
        }
    }

    @Override
    public void onBossDefeated(BossEntity b) {
        if (boss == b) {
            bossDefeated = true;
            addScore(1000);
            showToast("보스를 처치했습니다!", 2000);
        }
    }

    @Override
    public void onPlayerHit() {
        long now = SystemTimer.getTime();

        // 기체 #3: 방어막이 켜져 있으면 한 번은 막기
        if (selectedShipIndex == 2) {
            if (now < invulnUntil) {
                // 이미 무적
                return;
            }
            if (shieldActive) {
                shieldActive = false;
                invulnUntil  = now + 1000;
                showToast("Block!", 1200);
                return;
            }
        }

        // 일반 피격 처리
        lives = Math.max(0, lives - 1);
        if (lives > 0) {
            invulnUntil = now + 1000;
            showToast("Hit! 남은 목숨: " + lives, 1200);
            return;
        }

        // 목숨 모두 소진 → 게임 오버
        lastRunElapsedMs = (runStartTime == 0) ? -1 : (now - runStartTime);
        showToast("Out of lives! Try again?", 1500);

        // TODO: 여기서 Game에 "게임 오버" 콜백을 줘서 세이브/랭킹 처리해도 됨.
        // 예: game.onRunGameOverFromPlayScreen(this);
        game.setCurrentScreen(new MainMenuScreen(game));
    }

    @Override
    public void activateBombAt(int cx, int cy) {
        int baseRadius = 160;
        int radius     = baseRadius; // 강화 시스템까지 옮기고 싶으면 여기에 레벨 반영

        List<Entity> toRemove = new ArrayList<>();
        for (Entity e : entities) {
            if (e instanceof AlienEntity ||
                    e instanceof AlienShotEntity ||
                    e instanceof AsteroidEntity) {

                double ex = e.getX() + e.getWidth() / 2.0;
                double ey = e.getY() + e.getHeight() / 2.0;
                double dx = ex - cx;
                double dy = ey - cy;
                if (dx*dx + dy*dy <= (long) radius * radius) {
                    toRemove.add(e);
                }
            } else if (e instanceof BossEntity) {
                double ex = e.getX() + e.getWidth() / 2.0;
                double ey = e.getY() + e.getHeight() / 2.0;
                double dx = ex - cx;
                double dy = ey - cy;
                if (dx*dx + dy*dy <= (long) radius * radius) {
                    ((BossEntity) e).takeDamage(80);
                }
            }
        }

        for (Entity e : toRemove) {
            if (e instanceof AlienEntity) {
                onAlienKilledAt(-1, -1);
            }
            removeEntity(e);
        }

        showToast("BOOM!", 700);
    }

    @Override
    public boolean isPlayerInvincible() {
        return SystemTimer.getTime() < invulnUntil;
    }

    @Override
    public boolean collectBomb() {
        if (bombCount < bombMax) {
            bombCount++;
            showToast("폭탄 획득! (" + bombCount + "/" + bombMax + ")", 800);
            return true;
        } else {
            showToast("폭탄이 가득 찼어요!", 800);
            return false;
        }
    }

    @Override
    public boolean collectLaser() {
        if (laserCount < laserMax) {
            laserCount = 1;
            showToast("레이저 획득! (L키로 사용)", 900);
            return true;
        } else {
            showToast("레이저는 1개만 소지 가능!", 900);
            return false;
        }
    }

    @Override
    public int getShipCenterX() {
        if (ship == null) {
            return VIRTUAL_WIDTH / 2;
        }
        return ship.getX() + ship.getWidth() / 2;
    }

    @Override
    public int getVirtualWidth() {
        return VIRTUAL_WIDTH;
    }

    @Override
    public int getVirtualHeight() {
        return VIRTUAL_HEIGHT;
    }

    @Override
    public void requestLogicUpdate() {
        // AlienEntity 같은 애들이 "행 내리기 + 방향 전환"이 필요할 때 호출하는 훅
        if (world != null) {
            // World에 이런 메소드가 있으면 그대로 위임
            world.requestLogicUpdate();
        }
    }

    // ========================================================
    // Screen 구현부 (update/render/입력 처리 등)
    // ========================================================

    @Override
    public void update(long delta) {
        // 1) 입력 처리
        processPlayerInput(delta);


        world.moveAll(delta);

        long now = SystemTimer.getTime();

        spawnManager.update(now);

        world.handleCollisions();
        world.applyLogicIfNeeded();

        checkWinCondition();

        world.flushRemovals();

    }

    @Override
    public void render(Graphics2D g) {
        // 1) 엔티티 그리기
        for (Entity e : entities) {
            e.draw(g);
        }

        // 2) 보스 HP 바
        drawBossHP(g);

        // 3) 간단 HUD (목숨/폭탄/레이저/점수)
        drawHud(g);

        // 4) 토스트 메시지
        drawToast(g);
    }

    @Override
    public void onMouseClick(int mx, int my) {
        // 인게임에서 마우스 클릭이 필요하면 구현
    }

    @Override
    public void onKeyPressed(KeyEvent e) {
        input.onKeyPressed(e);
    }

    @Override
    public void onKeyReleased(KeyEvent e) {
        input.onKeyReleased(e);
    }

    // ========================================================
    // 인게임 로직 내부 메서드들
    // ========================================================

    private void processPlayerInput(long delta) {
        if (ship != null) {
            ship.setHorizontalMovement(0);
        }

        double effSpeed = moveSpeed;

        // 블랙홀 감속
        if (ship != null) {
            double cx = ship.getX() + ship.getWidth() / 2.0;
            double cy = ship.getY() + ship.getHeight() / 2.0;
            float scale = getBlackHoleSpeedScaleFor(cx, cy);
            effSpeed *= scale;
        }

        // 좌우 이동
        if (input.isLeft() && !input.isRight()) {
            if (ship != null) ship.setHorizontalMovement(-effSpeed);
        } else if (input.isRight() && !input.isLeft()) {
            if (ship != null) ship.setHorizontalMovement(effSpeed);
        }

        // 공격/폭탄/레이저
        if (input.isFire())  tryToFire();
        if (input.isBomb())  tryToFireBomb();
        if (input.isLaser()) tryToFireLaser();
    }

    private void tryToFire() {
        if (ship == null) return;
        if (System.currentTimeMillis() - lastFire < firingInterval) return;
        lastFire = System.currentTimeMillis();

        if (selectedShipIndex == 1) {
            ShotEntity L = new ShotEntity(this, "sprites/shot.gif",
                    (int) (ship.getX() + 4), (int) (ship.getY() - 30));
            ShotEntity R = new ShotEntity(this, "sprites/shot.gif",
                    (int) (ship.getX() + 16), (int) (ship.getY() - 30));
            entities.add(L);
            entities.add(R);
            shotsFiredRun += 2;
        } else {
            ShotEntity shot = new ShotEntity(this, "sprites/shot.gif",
                    (int) (ship.getX() + 10), (int) (ship.getY() - 30));
            entities.add(shot);
            shotsFiredRun += 1;
        }
    }

    /** B 키로 폭탄 발사 시 호출 */
    private void tryToFireBomb() {
        if (ship == null) return;

        if (bombCount <= 0) {
            showToast("폭탄이 없습니다!", 800);
            return;
        }

        long now = System.currentTimeMillis();
        if (now - lastBombFire < bombFireInterval) {
            return;
        }
        lastBombFire = now;

        int bx = (int) (ship.getX() + ship.getWidth() / 2.0 - 8);
        int by = (int) (ship.getY() - 20);

        BombEntity proj = new BombEntity(this, bx, by);
        proj.setMode(BombEntity.Mode.PROJECTILE);
        entities.add(proj);

        bombCount--;
    }

    private void tryToFireLaser() {
        if (ship == null) return;

        if (laserCount <= 0) {
            showToast("레이저가 없습니다!", 800);
            return;
        }
        if (System.currentTimeMillis() - lastLaserUse < laserCooldown) return;

        lastLaserUse = System.currentTimeMillis();
        laserCount   = 0;

        int cx = getShipCenterX();
        entities.add(LaserEntity.createActiveBeam(this, cx, 500));
        showToast("LASER!", 300);
    }

    public void tickLaserAt(int cx, int halfWidth) {
        int extra  = UpgradeBalance.laserHalfWidthWithUpgrade(0, game.getLvLaser());
        int killed = (world != null) ? world.tickLaserAt(cx, halfWidth, extra) : 0;

        for (int i = 0; i < killed; i++) {
            notifyAlienKilled();   // 이건 아래 2번에서 설명
        }
    }


    private void notifyAlienKilled() {
        // TODO: 점수 증가, 코인 증가, 업적 처리 등을 여기로 옮기기
    }

    private void addScore(int delta) {
        if (delta > 0) score += delta;
    }

    private void spawnBombItemAt(int x, int y) {
        entities.add(new BombEntity(this, x, y));
    }

    private void spawnLaserItemAt(int x, int y) {
        entities.add(LaserEntity.createDropItem(this, x, y));
    }

    public float getBlackHoleSpeedScaleFor(double cx, double cy) {
        if (world == null) return 1.0f;
        return world.getBlackHoleSpeedScaleFor(cx, cy);
    }

    private void checkWinCondition() {
        int aliveAliens = 0;
        for (Entity e : entities) {
            if (e instanceof AlienEntity) aliveAliens++;
        }
        if (aliveAliens == 0) {
            if (!bossSpawned) {
                spawnBoss();
                return;
            }
            if (bossDefeated) {
                notifyWin();
            }
        }
    }

    private void notifyWin() {
        long now     = SystemTimer.getTime();
        long elapsed = (runStartTime == 0) ? 0 : (now - runStartTime);
        lastRunElapsedMs = elapsed;

        int bonus     = lives * 500;
        int timeBonus = 0;
        if (elapsed > 0) {
            long sec = elapsed / 1000;
            timeBonus = Math.max(0, 1000 - (int) (sec * 10));
        }
        addScore(bonus + timeBonus);

        if (!achClear100 && shotsFiredRun <= 100) {
            achClear100 = true;
            showToast("도전과제 달성: 100발 안에 클리어!", 2500);
        }
        if (!achClear1Min && elapsed <= 60_000) {
            achClear1Min = true;
            showToast("도전과제 달성: 1분 안에 클리어!", 2500);
        }

        showToast("Well done! You Win!", 2000);

        // TODO: 여기서 Game에 "승리했다" 콜백을 줘서 세이브/랭킹/강화/코인 처리해도 됨.
        // 예: game.onRunWinFromPlayScreen(this, score, elapsed, totalKills, shotsFiredRun, lives, ach…);
        game.setCurrentScreen(new MainMenuScreen(game));
    }

    private void spawnBoss() {
        if (bossSpawned) return;
        int bx = (VIRTUAL_WIDTH - 120) / 2;
        int by = 60;
        boss = new BossEntity(this, bx, by);
        entities.add(boss);
        bossSpawned = true;
        showToast("보스 등장!", 1500);
    }

    // ========================================================
    // HUD / 토스트
    // ========================================================

    private void drawHud(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 14));

        g.drawString("Lives: " + lives + " / " + maxLives, 10, 20);
        g.drawString("Bomb: " + bombCount + "/" + bombMax + " (B)", 10, 40);
        g.drawString("Laser: " + laserCount + "/" + laserMax + " (L)", 10, 60);
        g.drawString("Score: " + score, 10, 80);
    }

    private void drawBossHP(Graphics2D g) {
        if (boss == null || boss.isDead()) return;

        int cur = boss.getHP();
        int max = boss.getMaxHP();

        int barW = 400;
        int barH = 14;
        int x    = (VIRTUAL_WIDTH - barW) / 2;
        int y    = 36;

        g.setColor(new Color(0, 0, 0, 160));
        g.fillRoundRect(x - 2, y - 2, barW + 4, barH + 4, 8, 8);

        double ratio = Math.max(0, Math.min(1.0, cur / (double) max));
        int fill     = (int) (barW * ratio);

        g.setColor(new Color(200, 50, 50));
        g.fillRoundRect(x, y, fill, barH, 8, 8);

        g.setColor(Color.WHITE);
        g.drawString("BOSS", x, y - 6);
    }

    private void drawToast(Graphics2D g) {
        if (toastText == null || SystemTimer.getTime() > toastUntil) {
            return;
        }

        g.setFont(new Font("SansSerif", Font.BOLD, 16));
        FontMetrics fm = g.getFontMetrics();
        int textW = fm.stringWidth(toastText);
        int textH = fm.getHeight();
        int x = (VIRTUAL_WIDTH - textW) / 2;
        int y = VIRTUAL_HEIGHT - 60;

        g.setColor(new Color(0, 0, 0, 150));
        g.fillRoundRect(x - 10, y - textH, textW + 20, textH + 10, 10, 10);

        g.setColor(Color.WHITE);
        g.drawString(toastText, x, y);
    }

    private void showToast(String msg, long durationMs) {
        toastText  = msg;
        toastUntil = SystemTimer.getTime() + durationMs;
    }


}
