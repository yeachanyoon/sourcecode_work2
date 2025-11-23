package org.newdawn.spaceinvaders;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.newdawn.spaceinvaders.entity.*;
import org.newdawn.spaceinvaders.screen.MainMenuScreen;
import org.newdawn.spaceinvaders.screen.PlayScreen;
import org.newdawn.spaceinvaders.screen.Screen;

public class Game extends Canvas {

    private Screen currentScreen;

    private String toastText = null;
    private long   toastUntil = 0;

    private int selectedShipIndex = -1;

    private boolean achKill10   = false;
    private boolean achClear100 = false;
    private boolean achClear1Min = false;

    private int score = 0;

    private Sprite ship1Sprite, ship2Sprite, ship3Sprite, ship3ProSprite;

    public static final int VIRTUAL_WIDTH  = 800;
	public static final int VIRTUAL_HEIGHT = 600;

	private BufferStrategy strategy;
	private boolean gameRunning = true;

	private long lastFpsTime;
	private int fps;
	private String windowTitle = "Space Invaders 102";
	private JFrame container;

	// === Fullscreen & scaling ===
	private boolean fullscreen = false;
	private java.awt.GraphicsDevice gfxDev =
			java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

    private SaveManager saveManager = new SaveManager();
    private SaveState saveData = new SaveState();

    private ReinforceSystem reinforceSystem;
    private ShopSystem shopSystem;

    // 스킨 정의용
    private static final ShopSystem.Skin[] BASE_SKINS = {
            new ShopSystem.Skin("Blue",  "sprites/ship_blue.gif",  800, true),  // 기본 보유
            new ShopSystem.Skin("Gold",  "sprites/ship_gold.gif", 1200, false),
            new ShopSystem.Skin("Green", "sprites/ship_green.gif", 900,  false)
    };

    private final boolean[] levelUnlocked = new boolean[5];
	private int selectedLevel = -1;

	// 미리보기/하트
	private Sprite heartFullSprite, heartEmptySprite;

    private HudRenderer hudRenderer;

	public static class RankRow { public final String name; public final int score; public RankRow(String n,int s){name=n;score=s;} }
	private static final int LEADERBOARD_MAX = 10;

	// 배점
	private static final int SCORE_ALIEN = 100;
	private static final int SCORE_AST   = 50;
	private static final int SCORE_ALIEN_SHOT = 25;

	/* ========================= 강화 상태/상수 ========================= */
	private int reinforcePoints = 0;
	private int lvSpeed = 0, lvFireRate = 0, lvShield = 0, lvBomb = 0, lvLaser = 0;

	/* ========================= 상점(스킨) ========================= */
	private static class Skin {
		final String name, spriteRef;
		final int price;
		boolean owned;
		Skin(String n, String ref, int price, boolean owned){this.name=n; this.spriteRef=ref; this.price=price; this.owned=owned;}
	}
	private Skin[] skins = new Skin[] {
			new Skin("Blue",  "sprites/ship_blue.gif",  800, false),
			new Skin("Gold",  "sprites/ship_gold.gif", 1200, false),
			new Skin("Green", "sprites/ship_green.gif", 900,  false)
	};
	private int selectedSkinIndex = 0;
	private int coins = 0;

	private static final int COIN_PER_ALIEN = 5;
	private static final int COIN_ON_CLEAR  = 200;

	public Game() {
		container = new JFrame("Space Invaders 102");

		JPanel panel = (JPanel) container.getContentPane();
		panel.setPreferredSize(new Dimension(VIRTUAL_WIDTH, VIRTUAL_HEIGHT));
		panel.setLayout(null);

		setBounds(0, 0, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
		panel.add(this);
		setIgnoreRepaint(true);

		container.pack();
		container.setResizable(true);
		container.setVisible(true);
		container.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		container.addWindowListener(new WindowAdapter() {
			@Override public void windowClosing(WindowEvent e) { saveNow(); }
		});

		addKeyListener(new KeyInputHandler());
		addMouseListener(new MouseAdapter() {
			@Override public void mouseClicked(MouseEvent e) {
				// 스케일/오프셋 보정하여 가상 좌표로 변환
				int vx = toVirtualX(e.getX());
				int vy = toVirtualY(e.getY());
				handleMouseClick(vx, vy);
			}
		});
		addMouseMotionListener(new MouseMotionAdapter() {});

		createBufferStrategy(2);
		strategy = getBufferStrategy();

		ship1Sprite    = SpriteStore.get().getSprite("sprites/ship.gif");
		ship2Sprite    = SpriteStore.get().getSprite("sprites/ship2.png");
		ship3Sprite    = SpriteStore.get().getSprite("sprites/ship3.png");
		ship3ProSprite = SpriteStore.get().getSprite("sprites/ship3(pro).png");
		heartFullSprite  = SpriteStore.get().getSprite("sprites/full_heart.png");
		heartEmptySprite = SpriteStore.get().getSprite("sprites/empty_heart.png");

        hudRenderer = new HudRenderer(heartFullSprite, heartEmptySprite);

        saveData = saveManager.load();
        reinforceSystem = ReinforceSystem.fromSave(saveData);
        shopSystem       = ShopSystem.fromSave(saveData, BASE_SKINS);
        applySaveToRuntime();
        currentScreen = new MainMenuScreen(this);
    }

    public int getVirtualWidth() { return VIRTUAL_WIDTH; }
    public int getVirtualHeight() { return VIRTUAL_HEIGHT; }

	// ====== 스케일/오프셋/좌표 변환 ======
	private double currentScale() {
		int cw = getWidth(), ch = getHeight();
		if (cw <= 0 || ch <= 0) return 1.0;
		double sx = cw / (double) VIRTUAL_WIDTH;
		double sy = ch / (double) VIRTUAL_HEIGHT;
		return Math.min(sx, sy);
	}

	private int offsetXForScale(double scale) {
		return (int) Math.round((getWidth() - VIRTUAL_WIDTH * scale) / 2.0);
	}
	private int offsetYForScale(double scale) {
		return (int) Math.round((getHeight() - VIRTUAL_HEIGHT * scale) / 2.0);
	}

	private int toVirtualX(int mx) {
		double s = currentScale();
		int ox = offsetXForScale(s);
		return (int) Math.floor((mx - ox) / s);
	}
	private int toVirtualY(int my) {
		double s = currentScale();
		int oy = offsetYForScale(s);
		return (int) Math.floor((my - oy) / s);
	}

	private void rebuildBufferStrategy() {
		createBufferStrategy(2);
		strategy = getBufferStrategy();
	}

	private void setFullscreen(boolean on) {
		if (on == fullscreen) return;
		fullscreen = on;

		container.dispose();
		container.setUndecorated(on);

		if (on) {
			gfxDev.setFullScreenWindow(container);
			java.awt.DisplayMode dm = gfxDev.getDisplayMode();
			setBounds(0, 0, dm.getWidth(), dm.getHeight());
		} else {
			gfxDev.setFullScreenWindow(null);
			setBounds(0, 0, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
			container.pack();
			container.setLocationRelativeTo(null);
		}

		container.setVisible(true);
		rebuildBufferStrategy();
	}

	private void drawToast(Graphics2D g) {
        // 문자 자체로 표시 여부 판단
        if (toastText == null || SystemTimer.getTime() > toastUntil) {
            return;
        }

        g.setFont(new Font("SansSerif", Font.BOLD, 16));
        FontMetrics fm = g.getFontMetrics();
        int textW = fm.stringWidth(toastText);
        int textH = fm.getHeight();
        int x = (VIRTUAL_WIDTH - textW) / 2;
        int y = VIRTUAL_HEIGHT - 60;

        g.setColor(new Color(0, 0, 0, 150));
        g.fillRoundRect(x - 10, y - textH, textW + 20, textH + 10, 10, 10);

        g.setColor(Color.white);
        g.drawString(toastText, x, y);
	}

	private void drawCenteredString(Graphics2D g, String text, int width, int y) {
		FontMetrics fm = g.getFontMetrics();
		int x = (width - fm.stringWidth(text)) / 2;
		g.drawString(text, x, y);
	}

    private void handleMouseClick(int mx, int my) {
        // 메뉴/선택 화면이 켜져 있으면 Screen 쪽에 위임
        if (currentScreen != null) {
            currentScreen.onMouseClick(mx, my);
            return;
        }

        // 현재는 인게임 상태에서는 별도 마우스 처리 안 하니까 여기 비워둬도 됨
    }

    public void gameLoop() {
        long lastLoopTime = SystemTimer.getTime();
        while (gameRunning) {
            long now   = SystemTimer.getTime();
            long delta = now - lastLoopTime;
            lastLoopTime = now;

            updateFps(delta);

            Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
            prepareBackBuffer(g);

            double s = currentScale();
            int ox = offsetXForScale(s);
            int oy = offsetYForScale(s);
            Graphics2D vg = createVirtualGraphics(g, s, ox, oy);

            if (currentScreen != null) {
                currentScreen.update(delta);   // ← 로직
                currentScreen.render(vg);
            }

            drawToast(vg);

            vg.dispose();
            g.dispose();
            strategy.show();

            SystemTimer.sleep(10);
        }
    }

    /** FPS 계산 및 윈도우 타이틀 갱신 */
    private void updateFps(long delta) {
        lastFpsTime += delta;
        fps++;

        if (lastFpsTime >= 1000) {
            container.setTitle(windowTitle + " (FPS: " + fps + ")");
            lastFpsTime = 0;
            fps = 0;
        }
    }

    /** 실제 창 크기 기준 전체 백버퍼를 초기화(검정색) */
    private void prepareBackBuffer(Graphics2D g) {
        g.setColor(Color.black);
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    /**
     * 가상 해상도(VIRTUAL_WIDTH x VIRTUAL_HEIGHT)에 맞춘 Graphics2D 생성
     * - 스케일/오프셋 적용
     * - 가상 화면 영역을 검정으로 클리어
     */
    private Graphics2D createVirtualGraphics(Graphics2D g, double scale, int ox, int oy) {
        Graphics2D vg = (Graphics2D) g.create();
        vg.translate(ox, oy);
        vg.scale(scale, scale);

        vg.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 가상 화면 클리어
        vg.setColor(Color.black);
        vg.fillRect(0, 0, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);

        return vg;
    }

    /* ===== 입력 ===== */
	private class KeyInputHandler extends KeyAdapter {
		public void keyPressed(KeyEvent e) {
            // F11은 여전히 Game 직접 처리
            if (e.getKeyCode() == KeyEvent.VK_F11) {
                setFullscreen(!fullscreen);
                return;
            }
            if(currentScreen != null) {
                currentScreen.onKeyPressed(e);
            }
		}
		public void keyReleased(KeyEvent e) {
            if (currentScreen != null) {
                currentScreen.onKeyReleased(e);
            }
		}
	}

    public void setCurrentScreen(Screen s) {
        this.currentScreen = s;
    }

    public SaveState getSaveData() { return saveData; }
    public void showToastFromScreen(String msg, long ms) { showToast(msg, ms); }

    public void startGameFromMenu(int level, int shipIndex) {
        this.selectedLevel = level;
        this.selectedShipIndex = shipIndex;
        saveData.lastSelectedShipIndex = shipIndex;
        saveNow();
        this.currentScreen = new PlayScreen(this, level, shipIndex);
    }

    public void tryUpgradeSpeedFromScreen()   { tryUpgrade(() -> lvSpeed++ , lvSpeed); }
    public void tryUpgradeFireRateFromScreen(){ tryUpgrade(() -> lvFireRate++ , lvFireRate); }
    public void tryUpgradeBombFromScreen()    { tryUpgrade(() -> lvBomb++ , lvBomb); }
    public void tryUpgradeLaserFromScreen()   { tryUpgrade(() -> lvLaser++ , lvLaser); }

    public int  getSkinCount()          { return skins.length; }
    public String getSkinName(int i)    { return skins[i].name; }
    public String getSkinSpriteRef(int i){ return skins[i].spriteRef; }
    public boolean isSkinOwned(int i)   { return skins[i].owned; }
    public String getSkinStatusText(int i) {
        Skin s = skins[i];
        if (!s.owned) return "가격: " + s.price;
        return (i == selectedSkinIndex) ? "장착중" : "보유";
    }
    public void tryBuySkinFromScreen(int idx) { tryBuySkin(idx); }
    public void equipSkinFromScreen(int idx) {
        if (!skins[idx].owned) return;
        selectedSkinIndex = idx;
        saveData.selectedSkinIndex = idx;
        saveNow();
        showToast("스킨 장착: " + skins[idx].name, 900);
    }


	private void addScore(int delta){ if (delta>0) score += delta; }

	private void submitScoreToLeaderboard(String name, int sc){
		if (name == null || name.isEmpty()) name = "PLAYER";
		saveData.leaderboard.add(new RankRow(name, sc));
		saveData.leaderboard.sort((a,b)-> Integer.compare(b.score, a.score));
		while (saveData.leaderboard.size() > LEADERBOARD_MAX) saveData.leaderboard.remove(saveData.leaderboard.size()-1);
		saveNow();
	}

	public int getScore(){ return score; }

    // 인게임 여부를 외부에서 쓸 수 있게 공개
    public boolean isInGame() {
        return currentScreen instanceof PlayScreen;
    }

    // 기존 시그니처를 깨지 않으면서 내부 구현만 변경
    public boolean isWaitingForMenu() {
        return currentScreen instanceof MainMenuScreen;
    }


    public List<RankRow> getTopScores(int limit){
		int n = Math.min(limit, saveData.leaderboard.size());
		return new ArrayList<>(saveData.leaderboard.subList(0, n));
	}

	private interface Inc { void go(); }

	private void tryUpgrade(Inc action, int currentLv) {
		if (currentLv >= UpgradeBalance.REINF_MAX) { showToast("이미 최대 레벨입니다.", 900); return; }
		if (reinforcePoints <= 0) { showToast("강화 포인트가 부족합니다.", 900); return; }
		reinforcePoints--;
		action.go();
		saveData.reinforcePoints = reinforcePoints;
		saveData.lvSpeed = lvSpeed; saveData.lvFireRate = lvFireRate; saveData.lvShield = lvShield;
		saveData.lvBomb = lvBomb;   saveData.lvLaser = lvLaser;
		saveNow();
	}

	private void tryBuySkin(int idx) {
		if (idx < 0 || idx >= skins.length) return;
		Skin s = skins[idx];
		if (s.owned) { showToast("이미 보유한 스킨입니다.", 900); return; }
		if (coins < s.price) { showToast("코인이 부족합니다.", 900); return; }
		coins -= s.price;
		s.owned = true;
		selectedSkinIndex = idx; // 구매 즉시 장착
		saveData.selectedSkinIndex = selectedSkinIndex;
		saveNow();
		showToast("구매/장착 완료: " + s.name + " (잔액: "+coins+")", 1300);
	}

    private void showToast(String msg, long durationMs) {
        toastText = msg;
        toastUntil = SystemTimer.getTime() + durationMs;
    }

    private void loadSave() {
        saveData = saveManager.load();
    }

    private void saveNow() {
        // 1) 강화/코인
        saveData.reinforcePoints = reinforcePoints;
        saveData.lvSpeed    = lvSpeed;
        saveData.lvFireRate = lvFireRate;
        saveData.lvShield   = lvShield;
        saveData.lvBomb     = lvBomb;
        saveData.lvLaser    = lvLaser;
        saveData.coins      = coins;

        // 2) 스킨 보유 상태 CSV로 직렬화
        StringBuilder ownedCsv = new StringBuilder();
        for (int i = 0; i < skins.length; i++) {
            if (i > 0) ownedCsv.append(',');
            ownedCsv.append(skins[i].owned ? "1" : "0");
        }
        saveData.ownedSkinsCsv    = ownedCsv.toString();
        saveData.selectedSkinIndex = selectedSkinIndex;

        // 3) 레벨 해금
        int highest = 1;
        for (int i = 0; i < 5; i++) {
            if (levelUnlocked[i]) highest = i + 1;
        }
        saveData.highestUnlockedLevel = highest;

        reinforceSystem.applyToSave(saveData);
        shopSystem.applyToSave(saveData);

        // 4) 실제 파일 저장
        saveManager.save(saveData);
    }

    private void resetSaveAndRuntime() {
        // 1) 저장 파일 초기화
        saveManager.reset();

        // 2) 메모리상의 saveData도 새로 만들기
        this.saveData = new SaveState();

        // 3) 강화/상점 시스템을 새 saveData 기반으로 다시 구성
        reinforceSystem = ReinforceSystem.fromSave(saveData);
        shopSystem      = ShopSystem.fromSave(saveData, BASE_SKINS);

        // 4) 세이브 내용을 Game의 필드에 반영
        applySaveToRuntime();

        // 5) 안내 토스트
        showToast("저장된 데이터가 초기화되었습니다.", 1400);
    }

    /** saveData(영구 저장 값)를 런타임 필드들에 적용 */
    private void applySaveToRuntime() {
        // 1) 레벨 해금 상태
        for (int i = 0; i < 5; i++) {
            levelUnlocked[i] = false;
        }
        int highest = Math.max(1, Math.min(5, saveData.highestUnlockedLevel));
        for (int i = 0; i < highest; i++) {
            levelUnlocked[i] = true;
        }

        // 2) 도전과제 플래그
        achKill10    = saveData.achKill10;
        achClear100  = saveData.achClear100;
        achClear1Min = saveData.achClear1Min;

        // 3) 마지막 선택 기체
        if (saveData.lastSelectedShipIndex >= 0 && saveData.lastSelectedShipIndex <= 2) {
            selectedShipIndex = saveData.lastSelectedShipIndex;
        } else {
            selectedShipIndex = -1;
        }

        // 4) 강화
        reinforcePoints = saveData.reinforcePoints;
        lvSpeed    = Math.min(UpgradeBalance.REINF_MAX, saveData.lvSpeed);
        lvFireRate = Math.min(UpgradeBalance.REINF_MAX, saveData.lvFireRate);
        lvShield   = Math.min(UpgradeBalance.REINF_MAX, saveData.lvShield);
        lvBomb     = Math.min(UpgradeBalance.REINF_MAX, saveData.lvBomb);
        lvLaser    = Math.min(UpgradeBalance.REINF_MAX, saveData.lvLaser);

        // 5) 코인/스킨
        coins = Math.max(0, saveData.coins);
        selectedSkinIndex = Math.max(0,
                Math.min(saveData.selectedSkinIndex, skins.length - 1));

        // 보유 스킨 배열 초기화
        for (int i = 0; i < skins.length; i++) {
            skins[i].owned = false;
        }
        if (saveData.ownedSkinsCsv != null && !saveData.ownedSkinsCsv.isEmpty()) {
            String[] bits = saveData.ownedSkinsCsv.split(",");
            for (int i = 0; i < Math.min(bits.length, skins.length); i++) {
                if ("1".equals(bits[i]) || "true".equalsIgnoreCase(bits[i])) {
                    skins[i].owned = true;
                }
            }
        }
        if (selectedSkinIndex < 0 || selectedSkinIndex >= skins.length) {
            selectedSkinIndex = 0;
        }
    }

    public int getLvLaser() {
        return lvLaser;
    }
    public int getLvBomb()  { return lvBomb; }

    public static void main(String[] args) { Game g = new Game(); g.gameLoop(); }
}
