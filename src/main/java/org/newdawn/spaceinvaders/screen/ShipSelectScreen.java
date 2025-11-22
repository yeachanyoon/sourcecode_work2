package org.newdawn.spaceinvaders.screen;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.Sprite;
import org.newdawn.spaceinvaders.SpriteStore;
import org.newdawn.spaceinvaders.screen.AbstractMenuScreen;
import org.newdawn.spaceinvaders.screen.LevelSelectScreen;

import java.awt.*;

/**
 * 기체 선택 화면
 * - 3개의 기체 중 하나 선택 후 게임 시작
 */
public class ShipSelectScreen extends AbstractMenuScreen {

    private final int levelToStart;

    private final Button backBtn = new Button("← 뒤로");
    private final Button[] selectBtns = new Button[3];

    private final Sprite[] previews = new Sprite[3];

    public ShipSelectScreen(Game game, int levelToStart) {
        super(game);
        this.levelToStart = levelToStart;

        backBtn.setBounds(20, 20, 100, 40);

        int centerY = 260;
        int[] xs = { 200, 400, 600 };
        int bw = 160, bh = 44, by = centerY + 60;
        for (int i = 0; i < 3; i++) {
            selectBtns[i] = new Button("선택하기");
            selectBtns[i].setBounds(xs[i] - bw / 2, by, bw, bh);
        }

        SpriteStore ss = SpriteStore.get();
        previews[0] = ss.getSprite("sprites/ship.gif");
        previews[1] = ss.getSprite("sprites/ship2.png");
        previews[2] = ss.getSprite("sprites/ship3.png");
    }

    @Override
    public void render(Graphics2D g) {
        g.setColor(new Color(20, 20, 20));
        g.fillRect(0, 0, Game.VIRTUAL_WIDTH, Game.VIRTUAL_HEIGHT);

        backBtn.draw(g);

        g.setColor(new Color(220, 220, 220));
        drawCenteredString(g, "기체 선택", 140,
                new Font("SansSerif", Font.BOLD, 28));

        drawCenteredString(g,
                "1) 기본  2) 이속 반 + 2연발  3) 이속 약간↓ + 30킬마다 방어막",
                170, new Font("SansSerif", Font.PLAIN, 14));

        int centerY = 260;
        int[] xs = { 200, 400, 600 };

        for (int i = 0; i < 3; i++) {
            g.setColor(new Color(255, 255, 255, 25));
            g.fillOval(xs[i] - 45, centerY - 30, 90, 60);
            if (previews[i] != null) {
                previews[i].draw(g, xs[i] - 16, centerY - 16);
            }
            selectBtns[i].draw(g);
        }
    }

    @Override
    public void onMouseClick(int mx, int my) {
        if (backBtn.contains(mx, my)) {
            game.setCurrentScreen(new LevelSelectScreen(game));
            return;
        }

        for (int i = 0; i < 3; i++) {
            if (selectBtns[i].contains(mx, my)) {
                int shipIndex = i;
                // Game 쪽에 "선택 상태 저장 + 실제 게임 시작"을 위임
                game.startGameFromMenu(levelToStart, shipIndex);
                return;
            }
        }
    }
}
