package org.newdawn.spaceinvaders;

import org.newdawn.spaceinvaders.entity.BossEntity;

import java.awt.*;

public class HudRenderer {
    public void drawHud(Graphics2D g, GameStats stats) {
        // 기존 drawLives + 폭탄/레이저/RP/Coins 문자열 렌더
    }

    public void drawBossHp(Graphics2D g, BossEntity boss) {
        // 기존 drawBossHP 로직
    }

    public void drawToast(Graphics2D g, ToastState toast) {
        // 기존 drawToast 로직
    }
}
