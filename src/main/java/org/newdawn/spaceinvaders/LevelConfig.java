package org.newdawn.spaceinvaders;

/**
 * 레벨별 밸런스 설정을 담는 클래스.
 * - 폭탄 드랍 확률
 * - 레이저 드랍 확률
 * - 에일리언 사격 간격
 * - 에일리언 이동 속도 배수
 */
public final class LevelConfig {

    public final double bombDropRate;
    public final double laserDropRate;
    public final long   alienFireIntervalMs;
    public final double alienSpeedMultiplier;

    public LevelConfig(double bombDropRate,
                       double laserDropRate,
                       long alienFireIntervalMs,
                       double alienSpeedMultiplier) {
        this.bombDropRate        = bombDropRate;
        this.laserDropRate       = laserDropRate;
        this.alienFireIntervalMs = alienFireIntervalMs;
        this.alienSpeedMultiplier = alienSpeedMultiplier;
    }

    /**
     * 레벨 번호(1~5)를 받아서 해당 레벨 설정을 반환.
     * 범위를 벗어나면 1레벨 설정을 기본으로 사용한다.
     */
    public static LevelConfig forLevel(int level) {
        switch (level) {
            case 1:

            case 2:
                return new LevelConfig(
                        0.08,
                        0.05,
                        1200,
                        0.90
                );
            case 3:
                return new LevelConfig(
                        0.07,
                        0.04,
                        1100,
                        1.00
                );
            case 4:
                return new LevelConfig(
                        0.06,
                        0.03,
                        1000,
                        1.10
                );
            case 5:
                return new LevelConfig(
                        0.05,
                        0.02,
                        900,
                        1.20
                );
            default:
                // 기존 Game.applyLevelParams 의 기본값
                return new LevelConfig(
                        0.10,  // bombDrop
                        0.06,  // laserDrop
                        1300,  // alienFireInterval
                        0.80   // alienSpeedMul
                );
        }
    }
}
