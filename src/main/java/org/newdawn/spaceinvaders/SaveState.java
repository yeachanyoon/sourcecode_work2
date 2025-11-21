package org.newdawn.spaceinvaders;

import java.util.ArrayList;
import java.util.List;

public class SaveState {

    // --- 레벨/도전과제 ---
    public int  highestUnlockedLevel = 1;  // 해금된 최고 레벨 (1~5)
    public boolean achKill10    = false;   // 적 10마리 처치
    public boolean achClear100  = false;   // 100발 이하 클리어
    public boolean achClear1Min = false;   // 1분 안에 클리어

    public int totalKillsOverall    = 0;   // 지금까지 처치한 적 수(최고 기록)
    public int bestTimeMs           = -1;  // 최단 클리어 타임(ms)
    public int lastSelectedShipIndex = -1; // 마지막 사용 기체 인덱스 (0~2)

    // --- 랭킹/플레이어 이름 ---
    public List<Game.RankRow> leaderboard = new ArrayList<>();
    public String playerName = System.getProperty("user.name", "PLAYER");

    // --- 강화 ---
    public int reinforcePoints = 0;
    public int lvSpeed    = 0;
    public int lvFireRate = 0;
    public int lvShield   = 0;
    public int lvBomb     = 0;
    public int lvLaser    = 0;

    // --- 상점/스킨 ---
    public int coins = 0;
    public int selectedSkinIndex = 0;
    /** 스킨 보유 여부를 "1,0,1,..." 형태로 저장 */
    public String ownedSkinsCsv = "";

    public SaveState() {
        // 기본값은 필드 선언부에서 이미 넣어놔서 비워둬도 됨
    }
}
