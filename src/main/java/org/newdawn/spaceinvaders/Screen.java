package org.newdawn.spaceinvaders;

import java.awt.*;

public interface Screen {
    void render(Graphics2D g);
    void onMouseClick(int mx, int my);
}
