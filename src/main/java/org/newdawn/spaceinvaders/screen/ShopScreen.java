package org.newdawn.spaceinvaders.screen;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.SaveState;
import org.newdawn.spaceinvaders.Sprite;
import org.newdawn.spaceinvaders.SpriteStore;

import java.awt.*;

/**
 * 스킨 상점 화면
 * - 실제 구매/장착 로직은 Game.tryBuySkinFromScreen / equipSkinFromScreen 에 위임
 */
public class ShopScreen extends AbstractMenuScreen {

    private final Button backBtn = new Button("← 뒤로");
    private final Button[] buyBtns;
    private final Button[] equipBtns;
    private static final String FONT_NAME = "SansSerif";

    public ShopScreen(Game game) {
        super(game);
        int skinCount = game.getSkinCount();
        buyBtns   = new Button[skinCount];
        equipBtns = new Button[skinCount];

        for (int i = 0; i < skinCount; i++) {
            buyBtns[i]   = new Button("구매");
            equipBtns[i] = new Button("장착");
        }
        layoutButtons();
    }

    private void layoutButtons() {
        backBtn.setBounds(20, 20, 100, 40);

        int startY = 220;
        int rowH = 90;
        int buyX = 520, equipX = 610, btnW = 70, btnH = 36;

        for (int i = 0; i < buyBtns.length; i++) {
            int y = startY + i * rowH;
            buyBtns[i].setBounds(buyX,   y, btnW, btnH);
            equipBtns[i].setBounds(equipX, y, btnW, btnH);
        }
    }

    @Override
    public void render(Graphics2D g) {
        g.setColor(new Color(15,15,18));
        g.fillRect(0,0,Game.VIRTUAL_WIDTH,Game.VIRTUAL_HEIGHT);

        backBtn.draw(g);

        SaveState s = game.getSaveData();
        int coins = (s != null ? s.coins : 0);

        g.setColor(new Color(230,230,230));
        drawCenteredString(g, "상점 (스킨)", 120,
                new Font(FONT_NAME, Font.BOLD, 28));

        drawCenteredString(g, "코인: " + coins + "   (적 처치/클리어로 획득)",
                150, new Font(FONT_NAME, Font.PLAIN, 16));

        int startY = 200;
        int rowH = 90;
        SpriteStore store = SpriteStore.get();

        for (int i = 0; i < game.getSkinCount(); i++) {
            int y = startY + i * rowH;

            g.setColor(new Color(255,255,255,20));
            g.fillRoundRect(140, y-10, 520, 70, 16, 16);
            g.setColor(new Color(255,255,255,50));
            g.drawRoundRect(140, y-10, 520, 70, 16, 16);

            Sprite preview = store.getSprite(game.getSkinSpriteRef(i));
            if (preview != null) preview.draw(g, 160, y);

            g.setColor(Color.WHITE);
            g.setFont(new Font(FONT_NAME, Font.BOLD, 16));
            g.drawString(game.getSkinName(i), 220, y+20);

            g.setFont(new Font(FONT_NAME, Font.PLAIN, 14));
            String status = game.getSkinStatusText(i); // "가격: 800", "보유", "장착중" 등
            g.drawString(status, 220, y+42);

            if (!game.isSkinOwned(i)) {
                buyBtns[i].draw(g);
            } else {
                equipBtns[i].draw(g);
            }
        }
    }

    @Override
    public void onMouseClick(int mx, int my) {
        if (backBtn.contains(mx, my)) {
            game.setCurrentScreen(new MainMenuScreen(game));
            return;
        }

        for (int i = 0; i < buyBtns.length; i++) {
            if (!game.isSkinOwned(i) && buyBtns[i].contains(mx, my)) {
                game.tryBuySkinFromScreen(i);
                return;
            }
            if (game.isSkinOwned(i) && equipBtns[i].contains(mx, my)) {
                game.equipSkinFromScreen(i);
                return;
            }
        }
    }
}
