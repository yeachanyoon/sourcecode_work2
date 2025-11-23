package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.GameContext;
import org.newdawn.spaceinvaders.Sprite;
import org.newdawn.spaceinvaders.SpriteStore;

public class AlienEntity extends PhysicalEntity {
    /** horizontal speed */
    private double moveSpeed = 75;
    /** game context (PlayScreen) */
    private final GameContext ctx;
    /** animation frames */
    private final Sprite[] frames = new Sprite[4];
    /** frame change timer */
    private long lastFrameChange;
    /** frame duration (ms) */
    private long frameDuration = 250;
    /** frame index */
    private int frameNumber;

    public AlienEntity(GameContext ctx, int x, int y) {
        super("sprites/alien.gif", x, y);

        // setup animation frames
        frames[0] = sprite;
        frames[1] = SpriteStore.get().getSprite("sprites/alien2.gif");
        frames[2] = sprite;
        frames[3] = SpriteStore.get().getSprite("sprites/alien3.gif");

        this.ctx = ctx;
        dx = -moveSpeed; // start moving left
    }

    @Override
    public void move(long delta) {
        // animation timing
        lastFrameChange += delta;
        if (lastFrameChange > frameDuration) {
            lastFrameChange = 0;
            frameNumber++;
            if (frameNumber >= frames.length) frameNumber = 0;
            sprite = frames[frameNumber];
        }

        // edge -> request logic update (row drop + reverse)
        if ((dx < 0) && (x < 10)) {
            ctx.requestLogicUpdate();     // ★ Game.updateLogic() 대신
        }
        if ((dx > 0) && (x > 750)) {
            ctx.requestLogicUpdate();
        }

        super.move(delta);
    }

    @Override
    public void doLogic() {
        // reverse horizontal movement and move down a bit
        dx = -dx;
        y += 10;

        // bottom reached -> player dies
        if (y > 570) {
            ctx.onPlayerHit();            // ★ game.notifyDeath() 대신
        }
    }

    @Override
    public void collidedWith(Entity other) {
        // no-op
    }
}
