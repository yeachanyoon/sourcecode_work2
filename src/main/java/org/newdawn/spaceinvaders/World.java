package org.newdawn.spaceinvaders;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.newdawn.spaceinvaders.entity.*;

/**
 * 엔티티 리스트를 가지고
 * - 이동(moveAll)
 * - 충돌 처리(handleCollisions)
 * - 로직 업데이트(applyLogicIfNeeded)
 * - 스폰/폭탄/레이저/블랙홀 처리
 * 를 담당하는 클래스.
 */
public class World {

    private final GameContext game;
    private final List<Entity> entities;
    private final List<Entity> removeList;
    private final SpawnManager spawnManager;
    private final Random rng = new Random();

    private boolean logicRequiredThisLoop = false;

    public World(GameContext game,
                 List<Entity> sharedEntities,
                 List<Entity> sharedRemoveList,
                 SpawnManager spawnManager) {
        this.game = game;
        this.entities = sharedEntities;
        this.removeList = sharedRemoveList;
        this.spawnManager = spawnManager;
    }

    // ===== 공용 접근 =====
    public List<Entity> getEntities() {
        return entities;
    }

    public void addEntity(Entity e) {
        entities.add(e);
    }

    public void removeEntity(Entity e) {
        removeList.add(e);
    }

    public void requestLogicUpdate() {
        logicRequiredThisLoop = true;
    }

    // ===== 프레임 단위 이동 =====
    public void moveAll(long delta) {
        for (int i = 0; i < entities.size(); i++) {
            Entity e = entities.get(i);

            // ★ 이동 가능한 객체인지 확인
            if (e instanceof Movable) {
                ((Movable) e).move(delta);
            }
        }
    }

    public void handleCollisions() {
        int size = entities.size();
        for (int i = 0; i < size; i++) {
            Entity me = entities.get(i);

            // ★ 내가 충돌 가능한 객체인가?
            if (me instanceof Collidable) {
                Collidable cMe = (Collidable) me;

                for (int j = i + 1; j < size; j++) {
                    Entity other = entities.get(j);

                    // ★ 상대방도 충돌 검사가 필요한 대상인가? (옵션)
                    // 보통 한쪽만 Collidable이어도 충돌 판정은 가능하지만,
                    // 로직상 양쪽 다 물리 객체일 때만 의미가 있다면 체크합니다.
                    if (cMe.collidesWith(other)) {
                        cMe.collidedWith(other);

                        // 상대방도 Collidable이라면 알림
                        if (other instanceof Collidable) {
                            ((Collidable) other).collidedWith(me);
                        }
                    }
                }
            }
        }
    }


    // ===== 논리 업데이트(행 이동 등) =====
    public void applyLogicIfNeeded() {
        if (!logicRequiredThisLoop) {
            return;
        }

        for (int i = 0; i < entities.size(); i++) {
            Entity e = entities.get(i);

            // ★ 로직 업데이트가 필요한 물리 엔티티인지 확인
            if (e instanceof PhysicalEntity) {
                ((PhysicalEntity) e).doLogic();
            }
        }

        logicRequiredThisLoop = false;
    }

    // ===== 제거 리스트 플러시 =====
    public void flushRemovals() {
        if (!removeList.isEmpty()) {
            entities.removeAll(removeList);
            removeList.clear();
        }
    }

    // =====================================================================
    // 2번: 스폰/폭탄/레이저/블랙홀 관련 로직
    // =====================================================================

    // --- 외계인 중 무작위 1마리 뽑기 ---
    private Entity pickRandomAlien() {
        List<Entity> aliens = new ArrayList<>();
        for (Entity e : entities) {
            if (e instanceof AlienEntity) {
                aliens.add(e);
            }
        }
        if (aliens.isEmpty()) return null;
        return aliens.get(rng.nextInt(aliens.size()));
    }

    /** 랜덤 외계인에게 총알 발사 */
    public boolean fireRandomAlienShot() {
        Entity shooter = pickRandomAlien();
        if (shooter == null) {
            return false;
        }
        int sx = shooter.getX() + 12;
        int sy = shooter.getY() + 20;
        entities.add(new AlienShotEntity(game, "sprites/shot.gif", sx, sy));
        return true;
    }

    /** 랜덤 위치에서 운석 스폰 (SpawnManager에서 호출) */
    public void spawnAsteroidRandom(Random externalRng) {
        Random r = (externalRng != null) ? externalRng : rng;

        int x = r.nextInt(Math.max(1, game.getVirtualWidth() - 32));
        int y = -32;
        double fallSpeed = 500;
        entities.add(new AsteroidEntity(game, x, y, fallSpeed));
    }

    /** 플레이어 주변에 블랙홀 스폰 (SpawnManager에서 호출) */
    public void spawnBlackHoleAroundPlayer(Random externalRng,
                                           float radius,
                                           float slowScale,
                                           long lifeMs) {
        Random r = (externalRng != null) ? externalRng : rng;

        int w = 48;
        int h = 48;

        double sx;
        double sy;

        Entity ship = findShip();
        if (ship != null) {
            sx = ship.getX() + ship.getWidth() / 2.0;
            sy = ship.getY() + ship.getHeight() / 2.0;
        } else {
            sx = game.getVirtualWidth() / 2.0;
            sy = game.getVirtualHeight() / 2.0;
        }

        double minD = 120.0;
        double maxD = 220.0;
        double ang  = r.nextDouble() * Math.PI * 2.0;
        double dist = minD + r.nextDouble() * (maxD - minD);

        int x = (int) Math.round(sx + Math.cos(ang) * dist) - w / 2;
        int y = (int) Math.round(sy + Math.sin(ang) * dist) - h / 2;

        if (x < 0) x = 0;
        if (y < 0) y = 0;
        if (x > game.getVirtualWidth()  - w) x = game.getVirtualWidth()  - w;
        if (y > game.getVirtualHeight() - h) y = game.getVirtualHeight() - h;

        entities.add(new BlackHoleEntity(game, x, y, radius, slowScale, lifeMs));
    }

    /** 현재 월드에서 ShipEntity 탐색 */
    private Entity findShip() {
        for (Entity e : entities) {
            if (e instanceof ShipEntity) {
                return e;
            }
        }
        return null;
    }

    /**
     * 폭탄 폭발 처리
     * @return 폭탄에 의해 죽은 Alien 수 (점수/도전과제 처리는 Game에서)
     */
    public int activateBombAt(int cx, int cy, int radius) {
        List<Entity> toRemove = new ArrayList<>();
        int aliensKilled = 0;

        long radiusSq = (long) radius * radius;

        for (Entity e : entities) {
            double ex = e.getX() + e.getWidth() / 2.0;
            double ey = e.getY() + e.getHeight() / 2.0;
            double dx = ex - cx;
            double dy = ey - cy;

            if (dx * dx + dy * dy > radiusSq) {
                continue;
            }

            if (e instanceof AlienEntity ||
                    e instanceof AlienShotEntity ||
                    e instanceof AsteroidEntity) {

                if (e instanceof AlienEntity) {
                    aliensKilled++;
                }
                toRemove.add(e);
            } else if (e instanceof BossEntity) {
                ((BossEntity) e).takeDamage(80);
            }
        }

        for (Entity e : toRemove) {
            removeEntity(e);
        }

        return aliensKilled;
    }

    /**
     * 레이저 판정 처리
     * @param cx         레이저 중심 X
     * @param halfWidth  LaserEntity 기본 반폭
     * @param extraWidth 강화에 의한 추가 폭
     * @return 이번 프레임에 레이저로 죽은 Alien 수
     */
    public int tickLaserAt(int cx, int halfWidth, int extraWidth) {
        int left  = cx - (halfWidth + extraWidth);
        int right = cx + (halfWidth + extraWidth);

        List<Entity> toRemove = new ArrayList<>();
        int aliensKilled = 0;

        for (Entity e : entities) {
            int ex = e.getX() + e.getWidth() / 2;
            if (ex < left || ex > right) {
                continue;
            }

            if (e instanceof AlienEntity ||
                    e instanceof AlienShotEntity ||
                    e instanceof AsteroidEntity) {

                if (e instanceof AlienEntity) {
                    aliensKilled++;
                }
                toRemove.add(e);
            } else if (e instanceof BossEntity) {
                ((BossEntity) e).takeDamage(10);
            }
        }

        for (Entity e : toRemove) {
            removeEntity(e);
        }

        return aliensKilled;
    }

    /**
     * 플레이어 이동 속도에 적용할 블랙홀 감속 비율
     */
    public float getBlackHoleSpeedScaleFor(double cx, double cy) {
        for (Entity e : entities) {
            if (e instanceof BlackHoleEntity) {
                BlackHoleEntity bh = (BlackHoleEntity) e;

                double bx = e.getX() + e.getWidth() / 2.0;
                double by = e.getY() + e.getHeight() / 2.0;
                double dx = bx - cx;
                double dy = by - cy;
                double distSq = dx * dx + dy * dy;
                double r = bh.getRadius();

                if (distSq <= r * r) {
                    // 플레이어만 50% 이동
                    return 0.5f;
                }
            }
        }
        return 1.0f;
    }
}
