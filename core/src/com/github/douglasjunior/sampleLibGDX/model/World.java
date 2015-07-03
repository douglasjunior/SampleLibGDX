package com.github.douglasjunior.sampleLibGDX.model;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.github.douglasjunior.sampleLibGDX.MainApplication;

/**
 * Created by douglas on 02/07/15.
 */
public class World {

    private final Player player;
    private final MainApplication app;

    public World(MainApplication app) {
        this.app = app;
        this.player = new Player(this);
    }

    public Player getPlayer() {
        return player;
    }

    public void update(float delta) {
        handleInput(delta);
    }

    private void handleInput(float delta) {
        // Android controls
        if (Gdx.app.getType() == Application.ApplicationType.Android) {
//            if (Gdx.input.getAccelerometerY() < -2) player.strafeLeft(delta);
//            if (Gdx.input.getAccelerometerY() > 2) player.strafeRight(delta);
            if (Gdx.input.getAccelerometerX() < 7) player.moveForward(delta);
            if (Gdx.input.getAccelerometerX() > 9) player.moveBackward(delta);

            if (Gdx.input.isTouched()) {
                if (Gdx.input.getX() < Gdx.graphics.getWidth() / 2) player.turnLeft(delta);
                if (Gdx.input.getX() > Gdx.graphics.getWidth() / 2) player.turnRight(delta);
            }
        }

        // Desktop controls
        if (Gdx.app.getType() == Application.ApplicationType.Desktop) {
            if (Gdx.input.isKeyPressed(Input.Keys.UP)) player.moveForward(delta);
            if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) player.moveBackward(delta);
            if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) player.turnLeft(delta);
            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) player.turnRight(delta);
//            if(Gdx.input.isKeyPressed(Input.Keys.A)) player.strafeLeft(delta);
//            if(Gdx.input.isKeyPressed(Input.Keys.D)) player.strafeRight(delta);

        }
    }
}
