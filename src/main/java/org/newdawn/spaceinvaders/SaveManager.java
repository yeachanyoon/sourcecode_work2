package org.newdawn.spaceinvaders;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * SaveState <-> 파일(.properties) 간 직렬화/역직렬화 담당.
 */
public class SaveManager {

    private final File saveFile;

    public SaveManager() {
        this.saveFile = new File(
                System.getProperty("user.home", "."),
                ".space_invaders_save.properties"
        );
    }

    /** 세이브 파일을 읽어서 SaveState로 복원. 없으면 기본값이 들어있는 새 SaveState 반환. */
    public SaveState load() {
        SaveState state = new SaveState();

        if (!saveFile.exists()) {
            // 첫 실행이거나 저장 파일이 없을 때
            return state;
        }

        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(saveFile)) {
            props.load(in);
        } catch (IOException e) {
            // 깨진 파일 등 문제 있으면 그냥 기본값 사용
            e.printStackTrace();
            return state;
        }

        // ----- 기본 진행도 -----
        state.version               = readInt(props, "version", 1);
        state.playerName            = props.getProperty("playerName", "PLAYER");
        state.highestUnlockedLevel  = readInt(props, "highestUnlockedLevel", 1);
        state.totalKillsOverall     = readInt(props, "totalKillsOverall", 0);
        state.bestTimeMs            = readInt(props, "bestTimeMs", -1);
        state.lastSelectedShipIndex = readInt(props, "lastSelectedShipIndex", -1);

        // ----- 도전과제 -----
        state.achKill10    = readBool(props, "achKill10", false);
        state.achClear100  = readBool(props, "achClear100", false);
        state.achClear1Min = readBool(props, "achClear1Min", false);

        // ----- 강화 -----
        state.reinforcePoints = readInt(props, "reinforcePoints", 0);
        state.lvSpeed         = readInt(props, "lvSpeed", 0);
        state.lvFireRate      = readInt(props, "lvFireRate", 0);
        state.lvShield        = readInt(props, "lvShield", 0);
        state.lvBomb          = readInt(props, "lvBomb", 0);
        state.lvLaser         = readInt(props, "lvLaser", 0);

        // ----- 상점/스킨 -----
        state.coins            = readInt(props, "coins", 0);
        state.selectedSkinIndex = readInt(props, "selectedSkinIndex", 0);
        state.ownedSkinsCsv    = props.getProperty("ownedSkinsCsv", "1,0,0");

        // ----- 리더보드 -----
        String lb = props.getProperty("leaderboard", "");
        state.leaderboard.clear();
        if (!lb.isEmpty()) {
            String[] rows = lb.split("\\|");
            for (String row : rows) {
                String[] parts = row.split(":", 2);
                if (parts.length != 2) continue;
                String name = parts[0].trim();
                int score;
                try {
                    score = Integer.parseInt(parts[1].trim());
                } catch (NumberFormatException e) {
                    continue;
                }
                state.leaderboard.add(new Game.RankRow(name, score));
            }
        }

        return state;
    }

    /** SaveState 내용을 .properties 파일로 저장 */
    public void save(SaveState s) {
        Properties props = new Properties();

        // ----- 기본 진행도 -----
        props.setProperty("version",                String.valueOf(s.version));
        props.setProperty("playerName",             nvl(s.playerName, "PLAYER"));
        props.setProperty("highestUnlockedLevel",   String.valueOf(s.highestUnlockedLevel));
        props.setProperty("totalKillsOverall",      String.valueOf(s.totalKillsOverall));
        props.setProperty("bestTimeMs",             String.valueOf(s.bestTimeMs));
        props.setProperty("lastSelectedShipIndex",  String.valueOf(s.lastSelectedShipIndex));

        // ----- 도전과제 -----
        props.setProperty("achKill10",   String.valueOf(s.achKill10));
        props.setProperty("achClear100", String.valueOf(s.achClear100));
        props.setProperty("achClear1Min",String.valueOf(s.achClear1Min));

        // ----- 강화 -----
        props.setProperty("reinforcePoints", String.valueOf(s.reinforcePoints));
        props.setProperty("lvSpeed",   String.valueOf(s.lvSpeed));
        props.setProperty("lvFireRate",String.valueOf(s.lvFireRate));
        props.setProperty("lvShield",  String.valueOf(s.lvShield));
        props.setProperty("lvBomb",    String.valueOf(s.lvBomb));
        props.setProperty("lvLaser",   String.valueOf(s.lvLaser));

        // ----- 상점/스킨 -----
        props.setProperty("coins",             String.valueOf(s.coins));
        props.setProperty("selectedSkinIndex", String.valueOf(s.selectedSkinIndex));
        props.setProperty("ownedSkinsCsv",     nvl(s.ownedSkinsCsv, "1,0,0"));

        // ----- 리더보드 직렬화 -----
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.leaderboard.size(); i++) {
            Game.RankRow r = s.leaderboard.get(i);
            if (i > 0) sb.append("|");
            // 이름에 ':' 나 '|' 안쓴다는 가정
            sb.append(r.name).append(":").append(r.score);
        }
        props.setProperty("leaderboard", sb.toString());

        // 실제 파일에 쓰기
        try (FileOutputStream out = new FileOutputStream(saveFile)) {
            props.store(out, "Space Invaders save");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** 세이브 파일 삭제 (완전 초기화) */
    public void reset() {
        if (saveFile.exists()) {
            // 실패해도 게임이 터지지 않게 예외는 무시
            // (원하면 로그만 찍어도 됨)
            saveFile.delete();
        }
    }

    // ===== 유틸 =====

    private static int readInt(Properties p, String key, int def) {
        String v = p.getProperty(key);
        if (v == null) return def;
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static boolean readBool(Properties p, String key, boolean def) {
        String v = p.getProperty(key);
        if (v == null) return def;
        return Boolean.parseBoolean(v.trim());
    }

    private static String nvl(String s, String def) {
        return (s == null) ? def : s;
    }
}
