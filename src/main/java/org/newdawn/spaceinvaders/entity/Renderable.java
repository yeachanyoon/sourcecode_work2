package org.newdawn.spaceinvaders.entity;
import java.awt.Graphics;

//화면에 그려질 수 있는 엔티티를 위한 인터페이스.

public interface Renderable {
    void draw(Graphics g);
}