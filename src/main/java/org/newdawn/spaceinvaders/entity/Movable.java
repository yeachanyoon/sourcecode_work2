package org.newdawn.spaceinvaders.entity;
// 이동 파트
public interface Movable {
    void move(long delta);
    void setHorizontalMovement(double dx);
    void setVerticalMovement(double dy);
    double getHorizontalMovement();
    double getVerticalMovement();
}
