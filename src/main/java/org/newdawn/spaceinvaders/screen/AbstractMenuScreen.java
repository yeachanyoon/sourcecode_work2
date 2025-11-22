package org.newdawn.spaceinvaders.screen;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.screen.Screen;

import java.awt.*;

/**
 * 메뉴 계열 화면의 공통 기반 클래스.
 * - 가운데 정렬 텍스트 그리기
 * - 간단한 버튼 컴포넌트
 */
public abstract class AbstractMenuScreen implements Screen {

    protected final Game game;

    protected AbstractMenuScreen(Game game) {
        this.game = game;
    }

    protected void drawCenteredString(Graphics2D g, String text, int y, Font font) {
        Font old = g.getFont();
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        int x = (Game.VIRTUAL_WIDTH - fm.stringWidth(text)) / 2;
        g.drawString(text, x, y);
        g.setFont(old);
    }

    /** 이 Screen 전용 간단 버튼 */
    protected static class Button {
        String label;
        int x, y, w, h;

        Button(String label) { this.label = label; }

        void setBounds(int x, int y, int w, int h) {
            this.x = x; this.y = y; this.w = w; this.h = h;
        }

        boolean contains(int mx, int my) {
            return mx >= x && mx <= x + w && my >= y && my <= y + h;
        }

        void draw(Graphics2D g) {
            g.setColor(new Color(255, 255, 255, 220));
            g.fillRoundRect(x, y, w, h, 20, 20);
            g.setColor(new Color(0, 0, 0, 220));
            g.drawRoundRect(x, y, w, h, 20, 20);

            Font old = g.getFont();
            g.setFont(new Font("SansSerif", Font.BOLD, 18));
            FontMetrics fm = g.getFontMetrics();
            int tx = x + (w - fm.stringWidth(label)) / 2;
            int ty = y + (h - fm.getHeight()) / 2 + fm.getAscent();
            g.drawString(label, tx, ty);
            g.setFont(old);
        }
    }
}
