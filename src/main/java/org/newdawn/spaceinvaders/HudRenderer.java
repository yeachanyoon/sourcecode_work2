package org.newdawn.spaceinvaders;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import org.newdawn.spaceinvaders.entity.BossEntity;

/**
 * 인게임 HUD(목숨, 폭탄/레이저, RP, 코인, 보스 HP 바)를 그려주는 전담 클래스.
 */
public class HudRenderer {

    private final Sprite heartFull;
    private final Sprite heartEmpty;

    public HudRenderer(Sprite heartFull, Sprite heartEmpty) {
        this.heartFull = heartFull;
        this.heartEmpty = heartEmpty;
    }

    /**
     * 인게임 HUD를 한 번에 그리는 메소드.
     */
    public void renderInGameHud(Graphics2D g,
                                int virtualWidth,
                                int maxLives,
                                int lives,
                                int bombCount, int bombMax,
                                int laserCount, int laserMax,
                                int reinforcePoints,
                                int coins,
                                BossEntity bossOrNull) {

        drawLivesAndResources(g, maxLives, lives,
                bombCount, bombMax,
                laserCount, laserMax,
                reinforcePoints, coins);

        drawBossHP(g, virtualWidth, bossOrNull);
    }

    /** 왼쪽 위/아래의 하트, 폭탄/레이저, RP, 코인 출력 */
    private void drawLivesAndResources(Graphics2D g,
                                       int maxLives,
                                       int lives,
                                       int bombCount, int bombMax,
                                       int laserCount, int laserMax,
                                       int reinforcePoints,
                                       int coins) {

        int x = 10;
        int y = 10;
        int gap = 5;

        for (int i = 0; i < maxLives; i++) {
            Sprite heart = (i < lives) ? heartFull : heartEmpty;
            if (heart != null) {
                heart.draw(g, x + i * (heart.getWidth() + gap), y);
            }
        }

        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        g.setColor(Color.WHITE);

        // 왼쪽 하단 텍스트들
        // (좌표는 기존 Game.drawLives()와 동일)
        g.drawString("Bomb: " + bombCount + "/" + bombMax + "  (B키)", 10, 560);
        g.drawString("Laser: " + laserCount + "/" + laserMax + "  (L키)", 10, 580);
        g.drawString("RP: " + reinforcePoints, 10, 78);
        g.drawString("Coins: " + coins, 10, 96);
    }

    /** 보스 HP 바 렌더링 */
    private void drawBossHP(Graphics2D g, int virtualWidth, BossEntity boss) {
        if (boss == null || boss.isDead()) {
            return;
        }

        int cur = boss.getHP();
        int max = boss.getMaxHP();

        int barW = 400;
        int barH = 14;
        int x = (virtualWidth - barW) / 2;
        int y = 36;

        g.setColor(new Color(0, 0, 0, 160));
        g.fillRoundRect(x - 2, y - 2, barW + 4, barH + 4, 8, 8);

        double ratio = Math.max(0, Math.min(1.0, cur / (double) max));
        int fill = (int) (barW * ratio);

        g.setColor(new Color(200, 50, 50));
        g.fillRoundRect(x, y, fill, barH, 8, 8);

        g.setColor(Color.WHITE);
        g.drawString("BOSS", x, y - 6);
    }
}
