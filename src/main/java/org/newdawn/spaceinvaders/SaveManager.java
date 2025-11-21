package org.newdawn.spaceinvaders;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import static com.sun.xml.internal.ws.message.Util.parseBool;
import static java.lang.Integer.parseInt;

public class SaveManager {
    private final File saveFile;

    public SaveManager() {
        this.saveFile = new File(System.getProperty("user.home", "."), ".space_invaders_save.properties");
    }

    /** 저장 파일을 읽어 SaveState로 복원 (파일 없거나 실패하면 기본값) */
    public SaveState load() {
        SaveState state = new SaveState();   // 기본값이 들어있는 상태

        if (!saveFile.exists()) {
            return state;
        }

        Properties p = new Properties();
        try (FileInputStream in = new FileInputStream(saveFile)) {
            p.load(in);
        } catch (IOException e) {
            e.printStackTrace();
            return state; // 깨진 파일이면 그냥 기본값 사용
        }

        // --- 기본 정보 ---
        state.playerName           = p.getProperty("playerName", state.playerName);
        state.highestUnlockedLevel = parseInt(p, "highestLevel", state.highestUnlockedLevel);
        state.bestTimeMs           = parseInt(p, "bestTimeMs",   state.bestTimeMs);
        state.totalKillsOverall    = parseInt(p, "totalKillsOverall", state.totalKillsOverall);

        // --- 도전과제 ---
        state.achKill10    = parseBool(p, "achKill10",    state.achKill10);
        state.achClear100  = parseBool(p, "achClear100",  state.achClear100);
        state.achClear1Min = parseBool(p, "achClear1Min", state.achClear1Min);

        // --- 강화 ---
        state.reinforcePoints = parseInt(p, "reinforcePoints", state.reinforcePoints);
        state.lvSpeed         = parseInt(p, "lvSpeed",         state.lvSpeed);
        state.lvFireRate      = parseInt(p, "lvFireRate",      state.lvFireRate);
        state.lvShield        = parseInt(p, "lvShield",        state.lvShield);
        state.lvBomb          = parseInt(p, "lvBomb",          state.lvBomb);
        state.lvLaser         = parseInt(p, "lvLaser",         state.lvLaser);

        // --- 상점/스킨 ---
        state.coins              = parseInt(p, "coins",             state.coins);
        state.selectedSkinIndex  = parseInt(p, "selectedSkinIndex", state.selectedSkinIndex);
        state.ownedSkinsCsv      = p.getProperty("ownedSkinsCsv",   state.ownedSkinsCsv);
        state.lastSelectedShipIndex =
                parseInt(p, "lastSelectedShipIndex", state.lastSelectedShipIndex);

        // --- 리더보드 ---
        state.leaderboard.clear();
        int lbCount = parseInt(p, "lb.count", 0);
        for (int i = 0; i < lbCount; i++) {
            String name = p.getProperty("lb." + i + ".name");
            int sc      = parseInt(p, "lb." + i + ".score", -1);
            if (name != null && sc >= 0) {
                state.leaderboard.add(new Game.RankRow(name, sc));
            }
        }

        return state;
    }

    /** SaveState를 .properties 파일로 저장 */
    public void save(SaveState state) {
        Properties p = new Properties();

        // --- 기본 정보 ---
        p.setProperty("playerName",           nz(state.playerName));
        p.setProperty("highestLevel",         String.valueOf(state.highestUnlockedLevel));
        p.setProperty("bestTimeMs",           String.valueOf(state.bestTimeMs));
        p.setProperty("totalKillsOverall",    String.valueOf(state.totalKillsOverall));

        // --- 도전과제 ---
        p.setProperty("achKill10",    String.valueOf(state.achKill10));
        p.setProperty("achClear100",  String.valueOf(state.achClear100));
        p.setProperty("achClear1Min", String.valueOf(state.achClear1Min));

        // --- 강화 ---
        p.setProperty("reinforcePoints", String.valueOf(state.reinforcePoints));
        p.setProperty("lvSpeed",         String.valueOf(state.lvSpeed));
        p.setProperty("lvFireRate",      String.valueOf(state.lvFireRate));
        p.setProperty("lvShield",        String.valueOf(state.lvShield));
        p.setProperty("lvBomb",          String.valueOf(state.lvBomb));
        p.setProperty("lvLaser",         String.valueOf(state.lvLaser));

        // --- 상점/스킨 ---
        p.setProperty("coins",             String.valueOf(state.coins));
        p.setProperty("selectedSkinIndex", String.valueOf(state.selectedSkinIndex));
        p.setProperty("ownedSkinsCsv",     nz(state.ownedSkinsCsv));
        p.setProperty("lastSelectedShipIndex",
                String.valueOf(state.lastSelectedShipIndex));

        // --- 리더보드 ---
        int lbCount = (state.leaderboard == null) ? 0 : state.leaderboard.size();
        p.setProperty("lb.count", String.valueOf(lbCount));
        for (int i = 0; i < lbCount; i++) {
            Game.RankRow row = state.leaderboard.get(i);
            p.setProperty("lb." + i + ".name",  nz(row.name));
            p.setProperty("lb." + i + ".score", String.valueOf(row.score));
        }

        // 부모 디렉터리 없으면 생성(보통은 user.home라 필요 없지만 안전하게)
        File parent = saveFile.getParentFile();
        if (parent != null && !parent.exists()) {
            // 실패해도 크게 상관 없으니 결과는 무시
            parent.mkdirs();
        }

        try (FileOutputStream out = new FileOutputStream(saveFile)) {
            p.store(out, "Space Invaders Save");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int parseInt(Properties p, String key, int def) {
        String v = p.getProperty(key);
        if (v == null) return def;
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException ex) {
            return def;
        }
    }

    private static boolean parseBool(Properties p, String key, boolean def) {
        String v = p.getProperty(key);
        if (v == null) return def;
        return Boolean.parseBoolean(v.trim());
    }

    private static String nz(String s) {
        return (s == null) ? "" : s;
    }

    public void reset() {
        if (saveFile.exists()) saveFile.delete();
    }
}
