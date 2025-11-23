package org.newdawn.spaceinvaders.entity;

import java.awt.Graphics;
import java.awt.Graphics2D;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.GameContext;
import org.newdawn.spaceinvaders.Sprite;
import org.newdawn.spaceinvaders.SpriteStore;
import org.newdawn.spaceinvaders.SystemTimer;

/**
 * BombEntity: DROP(떨어지는아이템) / PROJECTILE(발사체) / EXPLODING(폭발)
 */
public class BombEntity extends PhysicalEntity {

    private final GameContext ctx;   // ✅ Game 대신 인터페이스 하나만 사용

    public enum Mode { DROP, PROJECTILE, EXPLODING }

    private Mode mode;

    // 움직임 속도
    private final double dropSpeedY = 120;   // 천천히 낙하
    private final double shotSpeedY = -450;  // 위로 발사

    // 폭발 이펙트
    private final long explosionLifeMs = 550;
    private long explodeStart = -1;

    // 폭발 스프라이트
    private Sprite explosionSprite;

    public BombEntity(GameContext ctx, int x, int y) {
        super("sprites/bomb.png", x, y);
        this.ctx = ctx;
        this.explosionSprite = loadExplosionSprite();
        setMode(Mode.DROP); // 기본은 DROP
    }

    /** explosion 스프라이트 로드: png 우선, 실패 시 gif */
    private Sprite loadExplosionSprite() {
        SpriteStore store = SpriteStore.get();
        Sprite s = null;
        try { s = store.getSprite("sprites/explosion.png"); } catch (RuntimeException ignore) {}
        if (s == null) {
            try { s = store.getSprite("sprites/explosion.gif"); } catch (RuntimeException ignore) {}
        }
        return s;
    }

    public final void setMode(Mode m) {
        this.mode = m;
        switch (m) {
            case DROP:
                setHorizontalMovement(0);
                setVerticalMovement(dropSpeedY);
                break;
            case PROJECTILE:
                setHorizontalMovement(0);
                setVerticalMovement(shotSpeedY);
                break;
            case EXPLODING:
                setHorizontalMovement(0);
                setVerticalMovement(0);
                explodeStart = SystemTimer.getTime();
                // 폭발 논리는 GameContext.activateBombAt 에서 수행
                int cx = (int) (getX() + getWidth()  / 2.0);
                int cy = (int) (getY() + getHeight() / 2.0);
                ctx.activateBombAt(cx, cy);
                break;
        }
    }

    @Override
    public void move(long delta) {
        switch (mode) {
            case DROP:
                super.move(delta);
                if (getY() > Game.VIRTUAL_HEIGHT) {
                    ctx.removeEntity(this);       // ✅ ctx 사용
                }
                break;

            case PROJECTILE:
                super.move(delta);
                if (getY() + getHeight() < 0) {
                    setMode(Mode.EXPLODING);
                }
                break;

            case EXPLODING:
                if (SystemTimer.getTime() - explodeStart >= explosionLifeMs) {
                    ctx.removeEntity(this);       // ✅ ctx 사용
                }
                break;
        }
    }

    @Override public void doLogic() { }

    @Override
    public void collidedWith(Entity other) {
        if (mode == Mode.DROP) {
            if (other instanceof ShipEntity) {
                if (ctx.collectBomb()) {          // ✅ ctx 사용
                    ctx.removeEntity(this);
                }
            }
            return;
        }

        if (mode == Mode.PROJECTILE) {
            if (other instanceof AlienEntity ||
                    other instanceof AlienShotEntity ||
                    other instanceof AsteroidEntity) {
                setMode(Mode.EXPLODING);
            }
            return;
        }
        // EXPLODING 중에는 충돌 없음
    }

    @Override
    public void draw(Graphics g0) {
        Graphics2D g = (Graphics2D) g0;

        if (mode == Mode.EXPLODING) {
            if (explosionSprite != null) {
                int w = explosionSprite.getWidth();
                int h = explosionSprite.getHeight();
                int cx = (int) (getX() + getWidth()  / 2.0);
                int cy = (int) (getY() + getHeight() / 2.0);
                explosionSprite.draw(g, cx - w / 2, cy - h / 2);
            }
            return;
        }

        if (sprite != null && sprite.getWidth() > 0) {
            sprite.draw(g, (int) getX(), (int) getY());
        }
    }
}
