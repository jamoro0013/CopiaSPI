package com.spaceinvaders.spaceinvaders;

import javafx.scene.paint.Color;
import static com.spaceinvaders.spaceinvaders.SpaceInvaders.*;

// Bullet
public class Shot {

    public boolean toRemove;
    int posX, posY, speed = 10;
    static final int size = 6;

    public Shot(int posX, int posY) {
        this.posX = posX;
        this.posY = posY;
    }

    public void update() {
        posY-=speed;
    }

    public void draw() {
        gc.setFill(Color.RED);
        if (player.score >=50 && player.score<=70 || player.score>=120) {
            gc.setFill(Color.YELLOWGREEN);
            speed = 50;
            gc.fillRect(posX-5, posY-10, size+10, size+30);
        } else {
            gc.fillOval(posX, posY, size, size);
        }
    }

    public boolean colide(Rocket rocket) {
        int distance = distance(this.posX + size / 2, this.posY + size / 2,
                rocket.posX + rocket.size / 2, rocket.posY + rocket.size / 2);
        return distance  < rocket.size / 2 + size / 2;
    }

}