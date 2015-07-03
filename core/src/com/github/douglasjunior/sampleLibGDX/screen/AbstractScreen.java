package com.github.douglasjunior.sampleLibGDX.screen;

import com.badlogic.gdx.Screen;
import com.github.douglasjunior.sampleLibGDX.MainApplication;

/**
 * Created by douglas on 01/07/15.
 */
public abstract class AbstractScreen implements Screen {

    protected final MainApplication app;

    public AbstractScreen(MainApplication app) {
        this.app = app;
    }

    @Override
    public void hide() {
        dispose();
    }
}
