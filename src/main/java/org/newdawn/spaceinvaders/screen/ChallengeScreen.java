package org.newdawn.spaceinvaders.screen;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.SaveState;

import java.awt.*;

/**
 * 도전 과제 화면
 * - SaveState의 도전 과제 플래그를 기반으로 표시
 */
public class ChallengeScreen extends AbstractMenuScreen {

    private final Button backBtn = new Button("← 뒤로");

    private final boolean achKill10;
    private final boolean achClear100;
    private final boolean achClear1Min;

    public ChallengeScreen(Game game) {
        super(game);
        backBtn.setBounds(20, 20, 100, 40);

        SaveState s = game.getSaveData();
        this.achKill10    = (s != null && s.achKill10);
        this.achClear100  = (s != null && s.achClear100);
        this.achClear1Min = (s != null && s.achClear1Min);
    }

    @Override
    public void render(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(0, 0, Game.VIRTUAL_WIDTH, Game.VIRTUAL_HEIGHT);

        backBtn.draw(g);

        g.setColor(Color.WHITE);
        drawCenteredString(g, "도전 과제", 120,
                new Font("SansSerif", Font.BOLD, 28));

        g.setFont(new Font("SansSerif", Font.PLAIN, 18));
        int y = 180;
        g.drawString("• 적 10마리 처치"      + (achKill10    ? " ✅" : ""), 220, y); y += 30;
        g.drawString("• 100발 이하로 클리어" + (achClear100  ? " ✅" : ""), 220, y); y += 30;
        g.drawString("• 1분 안에 클리어"     + (achClear1Min ? " ✅" : ""), 220, y);
    }

    @Override
    public void onMouseClick(int mx, int my) {
        if (backBtn.contains(mx, my)) {
            game.setCurrentScreen(new MainMenuScreen(game));
        }
    }
}
