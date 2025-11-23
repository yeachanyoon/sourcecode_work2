package org.newdawn.spaceinvaders.entity;

import java.awt.Graphics;
import org.newdawn.spaceinvaders.Sprite;
import org.newdawn.spaceinvaders.SpriteStore;

/**
 * 모든 게임 오브젝트의 최상위 클래스.
 * 위치(x, y)와 렌더링(draw) 책임만 가집니다.
 * 물리 연산(move, collide)은 분리되었습니다.
 */
public abstract class Entity {
    protected Sprite sprite;
    protected double x;
    protected double y;

    public Entity(String ref, int x, int y) {
        if (ref != null) {
            this.sprite = SpriteStore.get().getSprite(ref);
        }
        this.x = x;
        this.y = y;
    }

    public void draw(Graphics g) {
        if (sprite != null) {
            sprite.draw(g, (int) x, (int) y);
        }
    }

    public int getX() { return (int) x; }
    public int getY() { return (int) y; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }

    public int getWidth() {
        return (sprite != null) ? sprite.getWidth() : 0;
    }

    public int getHeight() {
        return (sprite != null) ? sprite.getHeight() : 0;
    }
}