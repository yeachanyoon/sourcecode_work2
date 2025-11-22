package org.newdawn.spaceinvaders.screen;

import org.newdawn.spaceinvaders.Game;

import java.awt.*;

/**
 * 메인 메뉴 화면
 * - Play
 * - 도전 과제
 * - 상점
 * - 강화
 */
public class MainMenuScreen extends AbstractMenuScreen {

    private final Button playBtn       = new Button("Play");
    private final Button challengeBtn  = new Button("도전 과제");
    private final Button shopBtn       = new Button("상점");
    private final Button reinforceBtn  = new Button("강화");

    public MainMenuScreen(Game game) {
        super(game);

        int bw = 160, bh = 44;
        int gap = 12;
        int bx = (Game.VIRTUAL_WIDTH - bw) / 2;
        int by = 300;

        playBtn.setBounds(bx, by, bw, bh);
        challengeBtn.setBounds(bx, by + (bh + gap), bw, bh);
        shopBtn.setBounds(bx, by + (bh + gap) * 2, bw, bh);
        reinforceBtn.setBounds(bx, by + (bh + gap) * 3, bw, bh);
    }

    @Override
    public void render(Graphics2D g) {
        // 반투명 오버레이
        g.setColor(new Color(0, 0, 0, 170));
        g.fillRect(0, 0, Game.VIRTUAL_WIDTH, Game.VIRTUAL_HEIGHT);

        g.setColor(Color.WHITE);
        drawCenteredString(g, "SPACE INVADERS", 200,
                new Font("SansSerif", Font.BOLD, 36));

        drawCenteredString(g, "Play / 도전과제 / 상점 / 강화 버튼을 클릭하세요.",
                240, new Font("SansSerif", Font.PLAIN, 16));

        playBtn.draw(g);
        challengeBtn.draw(g);
        shopBtn.draw(g);
        reinforceBtn.draw(g);
    }

    @Override
    public void onMouseClick(int mx, int my) {
        if (playBtn.contains(mx, my)) {
            // 레벨 선택 화면으로 전환
            game.setCurrentScreen(new LevelSelectScreen(game));
            return;
        }
        if (challengeBtn.contains(mx, my)) {
            game.setCurrentScreen(new ChallengeScreen(game));
            return;
        }
        if (shopBtn.contains(mx, my)) {
            game.setCurrentScreen(new ShopScreen(game));
            return;
        }
        if (reinforceBtn.contains(mx, my)) {
            game.setCurrentScreen(new ReinforceScreen(game));
        }
    }
}
