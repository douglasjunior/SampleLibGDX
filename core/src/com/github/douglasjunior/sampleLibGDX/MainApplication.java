package com.github.douglasjunior.sampleLibGDX;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.github.douglasjunior.sampleLibGDX.screen.NavigationScreen;

public class MainApplication extends Game {

    @Override
    public void create() {
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();

        setScreen(new NavigationScreen(this));
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        System.out.println("Resize: " + width + " x " + height);
    }

}
