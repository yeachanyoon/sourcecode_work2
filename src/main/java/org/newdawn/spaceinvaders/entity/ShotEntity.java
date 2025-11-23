package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.GameContext;

/**
 * 플레이어가 쏘는 총알 엔티티
 */
public class ShotEntity extends PhysicalEntity {
    private static final double MOVE_SPEED = -300; // 위로 올라감

    // 🔹 이제 Game이 아니라 GameContext(PlayScreen 등)를 참조
    private final GameContext ctx;

    private boolean used = false;

    public ShotEntity(GameContext ctx, String sprite, int x, int y) {
        super(sprite, x, y);
        this.ctx = ctx;
        this.dy = MOVE_SPEED;
    }

    /** 이동 처리 */
    @Override
    public void move(long delta) {
        super.move(delta);

        // 화면 위로 벗어나면 제거
        if (y < -100) {
            ctx.removeEntity(this);   // GameContext.removeEntity(...)
        }
    }

    /** 충돌 처리 */
    @Override
    public void collidedWith(Entity other) {
        if (used) return;

        // 🔹 에일리언과 충돌 시
        if (other instanceof AlienEntity) {
            used = true;

            int cx = other.getX() + other.getWidth()  / 2;
            int cy = other.getY() + other.getHeight() / 2;

            ctx.removeEntity(this);
            ctx.removeEntity(other);
            ctx.onAlienKilledAt(cx, cy);
        }
    }

    /** 추상 메서드 구현 (Entity 상속 필수) */
    @Override
    public void doLogic() {
        // 총알은 별도의 논리 업데이트가 필요 없음
    }
}
