package org.newdawn.spaceinvaders.screen;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

public interface Screen {

    /** 매 프레임 호출되는 로직 (필요 없으면 구현 안 해도 됨) */
    default void update(long delta) {
        // 기본은 아무 것도 안 함
    }

    /** 반드시 구현해야 하는 렌더링 */
    void render(Graphics2D g);

    /** 마우스 클릭 (필요 없으면 오버라이드 안 해도 됨) */
    default void onMouseClick(int mx, int my) {
        // 기본은 무시
    }

    /** 키 눌림 (필요한 Screen만 오버라이드) */
    default void onKeyPressed(KeyEvent e) {
        // 기본은 무시
    }

    /** 키 떼짐 */
    default void onKeyReleased(KeyEvent e) {
        // 기본은 무시
    }
}

