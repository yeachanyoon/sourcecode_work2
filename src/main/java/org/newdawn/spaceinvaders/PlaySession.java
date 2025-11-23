package org.newdawn.spaceinvaders;

public final class PlaySession {
    private final GameContext context;   // 토스트, 엔티티 추가 등 필요하면 사용

    private int lives;
    private int score;
    private int bombCount;
    private int laserCount;
    private int totalKills;
    private long runStartTime;
    private boolean achKill10;
    // ... 생략: boss, shield, 도전과제 플래그 등

    public PlaySession(GameContext context, int maxLives) {
        this.context = context;
        this.lives = maxLives;
        this.runStartTime = SystemTimer.getTime();
    }

    public void onAlienKilled(int cx, int cy) {
        totalKills++;
        score += 100;
        // 드랍/도전과제/shield 처리 등을 여기에서
    }

    public void onPlayerHit() {
        // 목숨 감소, 무적 처리, 사망/게임오버 판단 등
    }

    public int getScore() { return score; }
    public int getLives() { return lives; }
}
