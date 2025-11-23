package org.newdawn.spaceinvaders.entity;

public interface Collidable {
    boolean collidesWith(Entity other);
    void collidedWith(Entity other);
}