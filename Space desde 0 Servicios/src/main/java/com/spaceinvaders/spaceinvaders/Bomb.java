package com.spaceinvaders.spaceinvaders;

import static com.spaceinvaders.spaceinvaders.SpaceInvaders.*;

// Enemy
public class Bomb extends Rocket {
    int SPEED = (score/5)+2;

    public Bomb(int posX, int posY, int size, int imgIndex) {
        super(posX, posY, size);
        this.imgIndex = imgIndex;
    }

    public void update() {
        super.update();
        if(!exploding && !destroyed) posY += SPEED;
        if(posY > HEIGHT) destroyed = true;
    }

    public void draw() {
        if(exploding) {
            gc.drawImage(EXPLOSION_IMG, explosionStep % EXPLOSION_COL * EXPLOSION_W, (explosionStep / EXPLOSION_ROWS) * EXPLOSION_H + 1,
                    EXPLOSION_W, EXPLOSION_H,
                    posX, posY, size, size);
        }
        else {
            gc.drawImage(BOMBS_IMG[imgIndex], posX, posY, size, size);
        }
    }
}