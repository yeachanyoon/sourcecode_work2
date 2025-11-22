package org.newdawn.spaceinvaders.screen;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.SaveState;

import java.awt.*;

/**
 * 레벨 선택 화면
 * - SaveState.highestUnlockedLevel 기준으로 잠김/해금 표시
 */
public class LevelSelectScreen extends AbstractMenuScreen {

    private final Button backBtn = new Button("← 뒤로");
    private final Button[] levelBtns = new Button[5];

    private final int highestUnlockedLevel;

    public LevelSelectScreen(Game game) {
        super(game);

        SaveState save = game.getSaveData();
        int highest = (save != null && save.highestUnlockedLevel > 0)
                ? save.highestUnlockedLevel : 1;
        this.highestUnlockedLevel = Math.min(5, Math.max(1, highest));

        for (int i = 0; i < levelBtns.length; i++) {
            levelBtns[i] = new Button("Level " + (i + 1));
        }

        layoutButtons();
    }

    private void layoutButtons() {
        backBtn.setBounds(20, 20, 100, 40);

        int bw = 160, bh = 44;
        int gap = 12;
        int bx = (Game.VIRTUAL_WIDTH - bw) / 2;
        int startY = 240;

        for (int i = 0; i < levelBtns.length; i++) {
            levelBtns[i].setBounds(bx, startY + i * (bh + gap), bw, bh);
        }
    }

    @Override
    public void render(Graphics2D g) {
        g.setColor(new Color(20, 20, 20));
        g.fillRect(0, 0, Game.VIRTUAL_WIDTH, Game.VIRTUAL_HEIGHT);

        backBtn.draw(g);

        g.setColor(new Color(220, 220, 220));
        drawCenteredString(g, "레벨 선택", 150,
                new Font("SansSerif", Font.BOLD, 28));

        drawCenteredString(g,
                "각 레벨을 클리어하면 다음 레벨이 해금됩니다.",
                180, new Font("SansSerif", Font.PLAIN, 14));

        for (int i = 0; i < levelBtns.length; i++) {
            levelBtns[i].draw(g);

            // 잠겨있으면 오버레이
            if (i + 1 > highestUnlockedLevel) {
                int x = levelBtns[i].x;
                int y = levelBtns[i].y;
                int w = levelBtns[i].w;
                int h = levelBtns[i].h;

                g.setColor(new Color(0, 0, 0, 140));
                g.fillRoundRect(x, y, w, h, 20, 20);

                g.setColor(Color.WHITE);
                Font old = g.getFont();
                g.setFont(new Font("SansSerif", Font.BOLD, 14));
                FontMetrics fm = g.getFontMetrics();
                String lock = "잠김";
                int tx = x + (w - fm.stringWidth(lock)) / 2;
                int ty = y + (h - fm.getHeight()) / 2 + fm.getAscent();
                g.drawString(lock, tx, ty);
                g.setFont(old);
            }
        }
    }

    @Override
    public void onMouseClick(int mx, int my) {
        if (backBtn.contains(mx, my)) {
            game.setCurrentScreen(new MainMenuScreen(game));
            return;
        }

        for (int i = 0; i < levelBtns.length; i++) {
            if (levelBtns[i].contains(mx, my)) {
                if (i + 1 <= highestUnlockedLevel) {
                    int level = i + 1;
                    game.setCurrentScreen(new ShipSelectScreen(game, level));
                } else {
                    game.showToastFromScreen("해금되지 않은 레벨입니다!", 1200);
                }
                return;
            }
        }
    }
}
