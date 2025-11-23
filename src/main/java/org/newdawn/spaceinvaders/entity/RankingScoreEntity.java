package org.newdawn.spaceinvaders.entity;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.List;

import org.newdawn.spaceinvaders.Game;

/** 도전과제 화면 하단에 Top Scores 표기하는 엔티티 */
public class RankingScoreEntity extends Entity {
    private final Game game;

    public RankingScoreEntity(Game game) {
        super("sprites/ship.gif", -10000, -10000); // 화면 밖
        this.game = game;

    }


    @Override
    public void draw(Graphics g) {
        // ✅ 더 이상 game.isShowingChallenge() 에 의존하지 않는다.
        //    이 엔티티를 도전과제 화면에서만 그리도록 Game/Screen 쪽에서 제어해라.

        if (game == null) return;

        g.setFont(new Font("SansSerif", Font.BOLD, 20));
        g.setColor(Color.WHITE);
        g.drawString("Top Scores", 250, 300);

        List<Game.RankRow> top = game.getTopScores(5);
        if (top == null || top.isEmpty()) return;

        g.setFont(new Font("SansSerif", Font.PLAIN, 16));
        FontMetrics fm = g.getFontMetrics();
        int yy = 330;

        for (int i = 0; i < top.size(); i++) {
            Game.RankRow r = top.get(i);
            String line = String.format("%2d) %-12s  %7d",
                    i + 1, r.name, r.score);
            g.drawString(line, 230, yy);
            yy += fm.getHeight() + 4;
        }
    }

}
