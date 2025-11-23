package org.newdawn.spaceinvaders;

/**
 * 강화 레벨에 따른 수치 계산을 모아 둔 유틸리티.
 * Game 안에 흩어져 있던 상수를 여기로 옮긴다.
 */
public final class UpgradeBalance {

    private UpgradeBalance() {}

    public static final int REINF_MAX = 5;

    // 속도/연사/폭탄/레이저 강화 상수 (Game 에 있던 것들)
    public static final double SPEED_PER_LV   = 0.10;
    public static final int    FIRE_PER_LV_MS = 40;
    public static final int    BOMB_RADIUS_PER_LV = 20;
    public static final int    LASER_HALF_PER_LV  = 4;

    /** 이동 속도 강화 적용 결과 계산 */
    public static double applySpeedUpgrade(double baseMoveSpeed, int lvSpeed) {
        return baseMoveSpeed * (1.0 + lvSpeed * SPEED_PER_LV);
    }

    /** 연사 강화 적용 결과 계산 (최소 100ms) */
    public static long applyFireRateUpgrade(long baseIntervalMs, int lvFireRate) {
        long result = baseIntervalMs - (long) lvFireRate * FIRE_PER_LV_MS;
        return Math.max(100, result);
    }

    /** 폭탄 반경 강화 적용 결과 계산 */
    public static int bombRadiusWithUpgrade(int baseRadius, int lvBomb) {
        return baseRadius + lvBomb * BOMB_RADIUS_PER_LV;
    }

    /** 레이저 반폭 강화 적용 결과 계산 */
    public static int laserHalfWidthWithUpgrade(int baseHalf, int lvLaser) {
        return baseHalf + lvLaser * LASER_HALF_PER_LV;
    }
}
