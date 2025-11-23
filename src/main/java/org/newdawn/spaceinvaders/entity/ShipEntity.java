package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.GameContext;

/**
 * The entity that represents the player's ship.
 */
public class ShipEntity extends Entity {
    /** The game context in which the ship exists (PlayScreen 등) */
    private final GameContext game;

    /**
     * Construct a new entity to represent the player's ship
     *
     * @param game The game context in which the ship is being created
     * @param ref  The reference to the sprite to show for the ship
     * @param x    The initial x location of the player's ship
     * @param y    The initial y location of the player's ship
     */
    public ShipEntity(GameContext game, String ref, int x, int y) {
        super(ref, x, y);
        this.game = game;
    }

    /**
     * Request that the ship move itself based on an elapsed amount of time
     *
     * @param delta The time that has elapsed since last move (ms)
     */
    @Override
    public void move(long delta) {
        // Prevent ship from leaving the window boundaries
        if ((dx < 0) && (x < 10)) {
            return;
        }
        if ((dx > 0) && (x > 750)) {
            return;
        }

        super.move(delta);
    }

    /**
     * Notification that the player's ship has collided with something
     *
     * @param other The entity with which the ship has collided
     */
    @Override
    public void collidedWith(Entity other) {
        // collisions with aliens or alien shots result in losing a heart
        if (other instanceof AlienEntity
                || other.getClass().getSimpleName().equals("AlienShotEntity")) {
            game.onPlayerHit();   // ★ 이전의 game.loseHeart() 대신 GameContext 메소드 호출
        }
    }

    @Override
    public void doLogic() {
        // Ship has no periodic logic to update
    }
}
