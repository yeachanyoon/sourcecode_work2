package org.newdawn.spaceinvaders.entity;

import java.awt.Graphics2D;

import org.newdawn.spaceinvaders.GameContext;
import org.newdawn.spaceinvaders.Sprite;
import org.newdawn.spaceinvaders.SpriteStore;

/** 보스 엔티티 */
public class BossEntity extends Entity {
    private final GameContext ctx;
    private final Sprite sprite;

    private int maxHP = 800;
    private int hp    = maxHP;

    private double speed = 120;
    private int leftBound  = 40;
    private int rightBound = 760;

    private long lastShot     = 0;
    private long shotInterval = 900;

    public BossEntity(GameContext ctx, int x, int y) {
        super("sprites/Boss.png", x, y);
        this.ctx    = ctx;
        this.sprite = SpriteStore.get().getSprite("sprites/Boss.png");
        setHorizontalMovement(speed);
        setVerticalMovement(0);
    }

    @Override
    public void move(long delta) {
        super.move(delta);

        if (getX() < leftBound) {
            setX(leftBound);
            setHorizontalMovement(Math.abs(getHorizontalMovement()));
        } else if (getX() + getWidth() > rightBound) {
            setX(rightBound - getWidth());
            setHorizontalMovement(-Math.abs(getHorizontalMovement()));
        }

        long now = System.currentTimeMillis();
        if (now - lastShot >= shotInterval) {
            lastShot = now;
            firePattern();
        }
    }

    /** 3갈래 탄막 */
    private void firePattern() {
        int cx = getX() + getWidth() / 2;
        int cy = getY() + getHeight() - 10;

        AlienShotEntity s0 = new AlienShotEntity(ctx, "sprites/shot.gif", cx, cy);
        s0.setHorizontalMovement(0);
        ctx.addEntity(s0);

        AlienShotEntity sL = new AlienShotEntity(ctx, "sprites/shot.gif", cx - 8, cy);
        sL.setHorizontalMovement(-80);
        ctx.addEntity(sL);

        AlienShotEntity sR = new AlienShotEntity(ctx, "sprites/shot.gif", cx + 8, cy);
        sR.setHorizontalMovement(+80);
        ctx.addEntity(sR);
    }

    public void takeDamage(int amount) {
        if (amount <= 0) return;
        hp -= amount;
        if (hp <= 0) {
            hp = 0;
            ctx.onBossDefeated(this);
            ctx.removeEntity(this);
        }
    }

    public boolean isDead()   { return hp <= 0; }
    public int  getHP()       { return hp; }
    public int  getMaxHP()    { return maxHP; }


    public void draw(Graphics2D g) {
        if (sprite != null) sprite.draw(g, getX(), getY());
    }

    @Override
    public void doLogic() { }

    @Override
    public void collidedWith(Entity other) {
        if (other instanceof ShotEntity) {
            takeDamage(10);
            ctx.removeEntity(other);
            return;
        }
        if (other instanceof ShipEntity) {
            ctx.onPlayerHit();
        }
    }

    public long getShotInterval() { return shotInterval; }

    public void setShotInterval(long ms) {
        this.shotInterval = Math.max(200, ms);
    }

    public void setSpeed(double pxPerSec) {
        this.speed = pxPerSec;
        setHorizontalMovement(
                Math.signum(getHorizontalMovement()) == 0
                        ? speed
                        : Math.copySign(speed, getHorizontalMovement())
        );
    }
}
