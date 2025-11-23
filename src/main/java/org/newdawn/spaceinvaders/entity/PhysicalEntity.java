package org.newdawn.spaceinvaders.entity;

import java.awt.Rectangle;

/**
 * 물리적 상호작용(이동, 충돌)을 하는 엔티티들의 기반 클래스
 */
public abstract class PhysicalEntity extends Entity implements Movable, Collidable {
    protected double dx;
    protected double dy;

    // 충돌 계산 최적화용 사각형 재사용
    private final Rectangle me = new Rectangle();
    private final Rectangle him = new Rectangle();

    public PhysicalEntity(String ref, int x, int y) {
        super(ref, x, y);
    }

    @Override
    public void move(long delta) {
        x += (dx * delta) / 1000.0;
        y += (dy * delta) / 1000.0;
    }

    @Override
    public boolean collidesWith(Entity other) {
        if (other == null || this == other) return false;

        // 시각적 요소가 없으면 물리 충돌 제외
        if (this.sprite == null || other.getWidth() == 0) return false;

        me.setBounds((int) x, (int) y, getWidth(), getHeight());
        him.setBounds(other.getX(), other.getY(), other.getWidth(), other.getHeight());

        return me.intersects(him);
    }

    // 속도 제어 구현
    @Override public void setHorizontalMovement(double dx) { this.dx = dx; }
    @Override public void setVerticalMovement(double dy) { this.dy = dy; }
    @Override public double getHorizontalMovement() { return dx; }
    @Override public double getVerticalMovement() { return dy; }

    // 게임 로직 업데이트 (AI 등)
    public abstract void doLogic();
}