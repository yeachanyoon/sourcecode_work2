package org.newdawn.spaceinvaders;

import java.util.List;
import org.newdawn.spaceinvaders.entity.Entity;

/**
 * 월드 업데이트 담당:
 * - 모든 Entity 이동
 * - 충돌 검사
 * - doLogic() 일괄 호출
 * - removeList 반영
 *
 * Game은 이 클래스를 통해 "엔티티 갱신" 책임을 위임한다.
 */
public class World {

    private final Game game;
    private final List<Entity> entities;
    private final List<Entity> removeList;

    /** AlienRow 떨어뜨리기 등 한 번에 doLogic() 호출이 필요함을 나타내는 플래그 */
    private boolean logicRequiredThisLoop = false;

    public World(Game game, List<Entity> entities, List<Entity> removeList, SpawnManager spawnManager) {
        this.game = game;
        this.entities = entities;
        this.removeList = removeList;
    }

    /** Game.updateLogic()에서 호출 → 다음 루프에서 doLogic() 일괄 호출하도록 표시 */
    public void requestLogicUpdate() {
        this.logicRequiredThisLoop = true;
    }

    /** delta(ms)에 맞춰 전체 엔티티 이동 */
    public void moveAll(long delta) {
        for (int i = 0; i < entities.size(); i++) {
            entities.get(i).move(delta);
        }
    }

    /** AABB 기반 충돌 검사 */
    public void handleCollisions() {
        int size = entities.size();
        for (int i = 0; i < size; i++) {
            Entity a = entities.get(i);
            for (int j = i + 1; j < size; j++) {
                Entity b = entities.get(j);
                if (a.collidesWith(b)) {
                    a.collidedWith(b);
                    b.collidedWith(a);
                }
            }
        }
    }

    /** logicRequiredThisLoop가 true이면 모든 엔티티의 doLogic() 호출 */
    public void applyLogicIfNeeded() {
        if (!logicRequiredThisLoop) return;

        for (int i = 0; i < entities.size(); i++) {
            entities.get(i).doLogic();
        }
        logicRequiredThisLoop = false;
    }

    /** Game.removeEntity()로 쌓아둔 removeList를 실제 entities에서 제거 */
    public void flushRemovals() {
        if (!removeList.isEmpty()) {
            entities.removeAll(removeList);
            removeList.clear();
        }
    }
}
