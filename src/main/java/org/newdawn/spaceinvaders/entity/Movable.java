package org.newdawn.spaceinvaders.entity;

public interface Movable {
    void move(long delta);
    void setHorizontalMovement(double dx);
    void setVerticalMovement(double dy);
    double getHorizontalMovement();
    double getVerticalMovement();
}