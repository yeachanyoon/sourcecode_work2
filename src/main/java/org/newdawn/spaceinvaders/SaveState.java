package org.newdawn.spaceinvaders;

import java.util.ArrayList;
import java.util.List;

/**
 * 게임의 영구 저장 데이터 모델.
 * - 순수 데이터만 들고 있고, 파일 IO는 SaveManager가 담당한다.
 */
public class SaveState {

    /** 버전 관리용 (필요하면 나중에 마이그레이션할 때 사용) */
    public int version = 1;

    /* ===== 기본 진행도 ===== */
    /** 플레이어 이름 (리더보드 표시용) */
    public String playerName = "PLAYER";

    /** 해금된 최고 레벨 (1~5) */
    public int highestUnlockedLevel = 1;

    /** 전체 누적 처치 수 (통계용) */
    public int totalKillsOverall = 0;

    /** 최고 클리어 타임(ms), 없으면 -1 */
    public int bestTimeMs = -1;

    /** 마지막으로 선택한 기체 인덱스 (0~2, 없으면 -1) */
    public int lastSelectedShipIndex = -1;

    /* ===== 도전과제 플래그 ===== */
    public boolean achKill10   = false;
    public boolean achClear100 = false;
    public boolean achClear1Min = false;

    /* ===== 강화 관련 ===== */
    public int reinforcePoints = 0;
    public int lvSpeed  = 0;
    public int lvFireRate = 0;
    public int lvShield = 0;
    public int lvBomb   = 0;
    public int lvLaser  = 0;

    /* ===== 상점/스킨 ===== */
    public int coins = 0;

    /** 현재 선택된 스킨 인덱스 (0 이상, 없으면 0) */
    public int selectedSkinIndex = 0;

    /**
     * 보유 스킨 정보 CSV.
     * 예: "1,0,0"  → 0번 스킨만 보유
     *     "1,1,0"  → 0,1번 스킨 보유
     */
    public String ownedSkinsCsv = "1,0,0";

    /* ===== 리더보드 ===== */
    public List<Game.RankRow> leaderboard = new ArrayList<>();
}
