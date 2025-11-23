package org.newdawn.spaceinvaders;

import java.awt.event.KeyEvent;

/**
 * 키 입력 상태를 관리하는 클래스.
 * Game에서는 이 객체의 상태만 보고 배를 움직이거나 공격한다.
 */
public class InputState {

    private boolean left;
    private boolean right;
    private boolean fire;
    private boolean bomb;
    private boolean laser;

    /** 모든 키 상태를 초기화 (메뉴에서 인게임으로 들어갈 때 등) */
    public void clear() {
        left = right = fire = bomb = laser = false;
    }

    /** KeyPressed 이벤트를 반영 */
    public void onKeyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        switch (code) {
            case KeyEvent.VK_LEFT:
                left = true;
                break;
            case KeyEvent.VK_RIGHT:
                right = true;
                break;
            case KeyEvent.VK_SPACE:
                fire = true;
                break;
            case KeyEvent.VK_B:
                bomb = true;
                break;
            case KeyEvent.VK_L:
                laser = true;
                break;
            default:
                // 다른 키는 무시
        }
    }

    /** KeyReleased 이벤트를 반영 */
    public void onKeyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        switch (code) {
            case KeyEvent.VK_LEFT:
                left = false;
                break;
            case KeyEvent.VK_RIGHT:
                right = false;
                break;
            case KeyEvent.VK_SPACE:
                fire = false;
                break;
            case KeyEvent.VK_B:
                bomb = false;
                break;
            case KeyEvent.VK_L:
                laser = false;
                break;
            default:
                // 다른 키는 무시
        }
    }

    // ===== Game에서 읽을 getter들 =====
    public boolean isLeft()  { return left; }
    public boolean isRight() { return right; }
    public boolean isFire()  { return fire; }
    public boolean isBomb()  { return bomb; }
    public boolean isLaser() { return laser; }
}
