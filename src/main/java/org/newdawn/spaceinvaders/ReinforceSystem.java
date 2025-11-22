package org.newdawn.spaceinvaders;

/**
 * 강화 수치와 강화 포인트를 관리하는 순수 로직 클래스.
 * - Game은 이 클래스를 필드로 들고 있다가
 *   - tryUpgradeXXX() 호출
 *   - getSpeedMultiplier(), getFireIntervalMs() 등으로 실제 값 계산
 * - SaveState와의 동기화는 fromSave/applyToSave 로 처리.
 */
public class ReinforceSystem {

    /** 강화 최대 레벨 */
    public static final int MAX_LEVEL = 5;

    private int points;
    private int lvSpeed;
    private int lvFireRate;
    private int lvShield;
    private int lvBomb;
    private int lvLaser;

    public ReinforceSystem() { }

    public static ReinforceSystem fromSave(SaveState s) {
        ReinforceSystem rf = new ReinforceSystem();
        rf.points    = s.reinforcePoints;
        rf.lvSpeed   = clamp(s.lvSpeed);
        rf.lvFireRate= clamp(s.lvFireRate);
        rf.lvShield  = clamp(s.lvShield);
        rf.lvBomb    = clamp(s.lvBomb);
        rf.lvLaser   = clamp(s.lvLaser);
        return rf;
    }

    /** 현재 상태를 SaveState에 반영 */
    public void applyToSave(SaveState s) {
        s.reinforcePoints = points;
        s.lvSpeed   = lvSpeed;
        s.lvFireRate= lvFireRate;
        s.lvShield  = lvShield;
        s.lvBomb    = lvBomb;
        s.lvLaser   = lvLaser;
    }

    private static int clamp(int lv) {
        if (lv < 0) return 0;
        if (lv > MAX_LEVEL) return MAX_LEVEL;
        return lv;
    }

    /* ===== 강화 시도 메서드 ===== */

    /** 공통 로직: 포인트 1 소모해서 주어진 레벨을 +1 한다. 성공 여부 반환. */
    private boolean tryUpgradeInternal(java.util.function.IntSupplier getter,
                                       java.util.function.IntConsumer setter) {
        int cur = getter.getAsInt();
        if (cur >= MAX_LEVEL) return false;
        if (points <= 0) return false;
        points--;
        setter.accept(cur + 1);
        return true;
    }

    public boolean tryUpgradeSpeed() {
        return tryUpgradeInternal(() -> lvSpeed, v -> lvSpeed = v);
    }

    public boolean tryUpgradeFireRate() {
        return tryUpgradeInternal(() -> lvFireRate, v -> lvFireRate = v);
    }

    public boolean tryUpgradeBomb() {
        return tryUpgradeInternal(() -> lvBomb, v -> lvBomb = v);
    }

    public boolean tryUpgradeLaser() {
        return tryUpgradeInternal(() -> lvLaser, v -> lvLaser = v);
    }

    public boolean tryUpgradeShield() {
        return tryUpgradeInternal(() -> lvShield, v -> lvShield = v);
    }

    /* ===== 실제 게임 수치 계산용 헬퍼 ===== */

    /** 이속 배수 (예: 기본 1.0 + 레벨당 0.10) */
    public double getSpeedMultiplier() {
        return 1.0 + lvSpeed * 0.10;
    }

    /** 기본 연사 간격에서 레벨당 40ms 감소 (최소 100ms) */
    public long getFiringIntervalMs(long baseIntervalMs) {
        long interval = baseIntervalMs - (long) lvFireRate * 40L;
        return Math.max(100L, interval);
    }

    /** 폭탄 반경 추가량 */
    public int getBombRadiusBonus() {
        return lvBomb * 20;
    }

    /** 레이저 폭 추가량 (halfWidth 기준) */
    public int getLaserHalfWidthBonus() {
        return lvLaser * 4;
    }

    /** 무적 시간 등 방어 관련 수치가 필요하면 여기에 추가 */
    public int getShieldLevel() {
        return lvShield;
    }

    /* ===== getter ===== */

    public int getPoints() { return points; }
    public void addPoints(int delta) { points = Math.max(0, points + delta); }

    public int getLvSpeed()    { return lvSpeed; }
    public int getLvFireRate() { return lvFireRate; }
    public int getLvShield()   { return lvShield; }
    public int getLvBomb()     { return lvBomb; }
    public int getLvLaser()    { return lvLaser; }
}
