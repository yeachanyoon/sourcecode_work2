package org.newdawn.spaceinvaders;

/**
 * 상점/스킨 로직 담당.
 * - 어떤 스킨이 있고, 가격/보유/장착 상태는 여기서 관리.
 * - Game은 코인 잔액과 현재 스킨 정보만 가져다 쓰면 된다.
 */
public class ShopSystem {

    public static class Skin {
        public final String name;
        public final String spriteRef;
        public final int price;
        public boolean owned;

        public Skin(String name, String spriteRef, int price, boolean owned) {
            this.name = name;
            this.spriteRef = spriteRef;
            this.price = price;
            this.owned = owned;
        }
    }

    /** 전체 스킨 목록 */
    private final Skin[] skins;

    /** 현재 선택된 스킨 인덱스 */
    private int selectedIndex = 0;

    /** 코인 잔액 */
    private int coins = 0;

    public ShopSystem(Skin[] baseSkins) {
        this.skins = baseSkins;
    }

    /** SaveState 에서 Shop 상태 복원 */
    public static ShopSystem fromSave(SaveState s, Skin[] baseSkins) {
        ShopSystem shop = new ShopSystem(baseSkins);
        shop.coins = Math.max(0, s.coins);
        shop.selectedIndex = Math.max(0,
                Math.min(s.selectedSkinIndex, baseSkins.length - 1));

        // ownedSkinsCsv → owned 플래그 적용
        if (s.ownedSkinsCsv != null && !s.ownedSkinsCsv.isEmpty()) {
            String[] bits = s.ownedSkinsCsv.split(",");
            for (int i = 0; i < Math.min(bits.length, baseSkins.length); i++) {
                if ("1".equals(bits[i]) || "true".equalsIgnoreCase(bits[i])) {
                    baseSkins[i].owned = true;
                }
            }
        }
        // 최소한 기본 스킨 0번은 항상 owned로
        if (!baseSkins[0].owned) baseSkins[0].owned = true;

        return shop;
    }

    /** 현재 상태를 SaveState에 반영 */
    public void applyToSave(SaveState s) {
        s.coins = coins;
        s.selectedSkinIndex = selectedIndex;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < skins.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(skins[i].owned ? "1" : "0");
        }
        s.ownedSkinsCsv = sb.toString();
    }

    /* ===== 구매/장착 로직 ===== */

    /**
     * idx번째 스킨을 구매 시도.
     *
     * @return 성공하면 true, 실패(이미 소유/코인부족/인덱스 오류)하면 false.
     */
    public boolean tryBuy(int idx) {
        if (!isValidIndex(idx)) return false;
        Skin s = skins[idx];
        if (s.owned) return false;
        if (coins < s.price) return false;

        coins -= s.price;
        s.owned = true;
        selectedIndex = idx;    // 구매 즉시 장착
        return true;
    }

    /**
     * 이미 보유한 스킨을 장착.
     */
    public boolean tryEquip(int idx) {
        if (!isValidIndex(idx)) return false;
        Skin s = skins[idx];
        if (!s.owned) return false;
        selectedIndex = idx;
        return true;
    }

    private boolean isValidIndex(int idx) {
        return idx >= 0 && idx < skins.length;
    }

    /* ===== getter/setter ===== */

    public Skin[] getSkins() { return skins; }

    public int getSelectedIndex() { return selectedIndex; }

    public Skin getSelectedSkin() {
        return skins[selectedIndex];
    }

    public int getCoins() { return coins; }
    public void addCoins(int delta) { coins = Math.max(0, coins + delta); }
}
