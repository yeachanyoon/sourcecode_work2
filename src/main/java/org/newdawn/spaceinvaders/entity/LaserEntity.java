package org.newdawn.spaceinvaders.entity;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;

import org.newdawn.spaceinvaders.GameContext;

/**
 * 레이저 아이템 & 레이저 빔(시각효과) 엔티티
 */
public class LaserEntity extends PhysicalEntity {

    public enum Mode { ITEM, BEAM }

    private final GameContext ctx;
    private final Mode mode;

    // ITEM 모드(도형)
    private static final int ITEM_W = 14;
    private static final int ITEM_H = 18;
    private static final int ITEM_FALL_SPEED = 120; // px/sec
    private boolean collected = false;

    // BEAM 모드
    private long expireAtMs;
    private int  beamHalfWidth = 1; // 시각적/판정 폭 절반
    private int  beamAlpha = 170;

    private LaserEntity(GameContext ctx, Mode mode, int x, int y) {
        super("sprites/shot.gif", x, y);
        this.ctx  = ctx;
        this.mode = mode;
        if (mode == Mode.BEAM) {
            this.sprite = null; // 스프라이트 렌더 안 쓰게
        }
    }

    /** 드랍되는 레이저 아이템 생성 (이미지 없이 도형) */
    public static LaserEntity createDropItem(GameContext ctx, int x, int y) {
        return new LaserEntity(ctx, Mode.ITEM, x, y);
    }

    /** 활성 레이저 빔 생성 (durationMs 유지) */
    public static LaserEntity createActiveBeam(GameContext ctx, int centerX, int durationMs) {
        LaserEntity e = new LaserEntity(ctx, Mode.BEAM, centerX, 0);
        long now = System.currentTimeMillis();
        e.expireAtMs = now + (durationMs <= 0 ? 500 : durationMs);
        return e;
    }

    public boolean isBeam() { return mode == Mode.BEAM; }

    @Override
    public void move(long delta) {
        if (mode == Mode.ITEM) {
            long dyLong = ((long) ITEM_FALL_SPEED * delta) / 1000L;
            this.y += (int) dyLong;
            if (this.y > ctx.getVirtualHeight() + 50) {
                ctx.removeEntity(this);
            }
        } else {
            int cx = ctx.getShipCenterX();
            this.x = cx - 1;
            this.y = 0;

            // 판정은 GameContext가 처리
            ctx.tickLaserAt(cx, beamHalfWidth);

            long now = System.currentTimeMillis();
            if (now >= expireAtMs) {
                ctx.removeEntity(this);
            }
        }
    }

    @Override
    public void draw(Graphics g) {
        if (mode == Mode.ITEM) {
            int drawX = (int) x;
            int drawY = (int) y;
            int cx = drawX + ITEM_W / 2;
            int cy = drawY + ITEM_H / 2;
            int hw = ITEM_W / 2;
            int hh = ITEM_H / 2;

            Polygon diamond = new Polygon(
                    new int[] { cx, cx + hw, cx, cx - hw },
                    new int[] { cy - hh, cy, cy + hh, cy },
                    4
            );
            g.setColor(new Color(255, 80, 80, 230));
            g.fillPolygon(diamond);
            g.setColor(new Color(255, 0, 0, 240));
            g.drawPolygon(diamond);
        } else {
            int cx = ctx.getShipCenterX();
            int left   = cx - beamHalfWidth;
            int width  = beamHalfWidth * 2;
            int height = ctx.getVirtualHeight();

            g.setColor(new Color(255, 0, 0, beamAlpha));
            g.fillRect(left, 0, width, height);
            g.setColor(new Color(255, 200, 200, beamAlpha));
            g.drawRect(left, 0, width, height);
        }
    }

    @Override
    public boolean collidesWith(Entity other) {
        if (mode == Mode.BEAM) return false;
        if (other == null)     return false;
        if (other instanceof LaserEntity) {
            LaserEntity le = (LaserEntity) other;
            if (le.mode == Mode.BEAM) return false;
        }
        if (this.sprite == null || other.sprite == null) return false;
        return super.collidesWith(other);
    }

    @Override
    public void collidedWith(Entity other) {
        if (mode == Mode.ITEM) {
            if (collected) return;
            if (other instanceof ShipEntity) {
                if (ctx.collectLaser()) {
                    collected = true;
                    ctx.removeEntity(this);
                }
            }
        }
    }

    @Override
    public void doLogic() { }

    @Override
    public int getWidth()  { return (mode == Mode.ITEM) ? ITEM_W : beamHalfWidth * 2; }

    @Override
    public int getHeight() { return (mode == Mode.ITEM) ? ITEM_H : ctx.getVirtualHeight(); }
}
