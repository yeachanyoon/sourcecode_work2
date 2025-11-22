package org.newdawn.spaceinvaders.screen;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.SaveState;

import java.awt.*;

/**
 * 강화 화면 (이속/연사/폭탄/레이저 업그레이드)
 * 실제 강화 로직은 Game.tryUpgradeXXX(...)에 위임.
 */
public class ReinforceScreen extends AbstractMenuScreen {

    private final Button speedBtn    = new Button("+이속");
    private final Button fireRateBtn = new Button("+연사");
    private final Button bombBtn     = new Button("+폭탄");
    private final Button laserBtn    = new Button("+레이저");
    private final Button backBtn     = new Button("완료");

    public ReinforceScreen(Game game) {
        super(game);
        layoutButtons();
    }

    private void layoutButtons() {
        int bw = 100, bh = 48;
        int gap = 22;

        int totalW = bw * 2 + gap;
        int baseX = (Game.VIRTUAL_WIDTH - totalW) / 2;
        int baseY = 240;

        speedBtn.setBounds   (baseX,             baseY,          bw, bh);
        fireRateBtn.setBounds(baseX + bw + 20,  baseY,          bw, bh);
        bombBtn.setBounds    (baseX,             baseY + bh+gap, bw, bh);
        laserBtn.setBounds   (baseX + bw + 20,  baseY + bh+gap, bw, bh);

        backBtn.setBounds((Game.VIRTUAL_WIDTH - 200)/2,
                baseY + (bh+gap)*2 + 30, 200, 52);
    }

    @Override
    public void render(Graphics2D g) {
        g.setColor(new Color(0,0,0,200));
        g.fillRect(0,0,Game.VIRTUAL_WIDTH,Game.VIRTUAL_HEIGHT);

        g.setColor(Color.WHITE);
        drawCenteredString(g, "강화 (Reinforce)", 150,
                new Font("SansSerif", Font.BOLD, 28));

        SaveState s = game.getSaveData();
        int rp = (s != null ? s.reinforcePoints : 0);
        drawCenteredString(g, "강화 포인트(RP): " + rp + "   (최대 레벨: 5)",
                180, new Font("SansSerif", Font.PLAIN, 16));

        speedBtn.draw(g);
        fireRateBtn.draw(g);
        bombBtn.draw(g);
        laserBtn.draw(g);
        backBtn.draw(g);
    }

    @Override
    public void onMouseClick(int mx, int my) {
        if (backBtn.contains(mx, my)) {
            game.setCurrentScreen(new MainMenuScreen(game));
            return;
        }

        if (speedBtn.contains(mx, my)) {
            game.tryUpgradeSpeedFromScreen();
            return;
        }
        if (fireRateBtn.contains(mx, my)) {
            game.tryUpgradeFireRateFromScreen();
            return;
        }
        if (bombBtn.contains(mx, my)) {
            game.tryUpgradeBombFromScreen();
            return;
        }
        if (laserBtn.contains(mx, my)) {
            game.tryUpgradeLaserFromScreen();
        }
    }
}
