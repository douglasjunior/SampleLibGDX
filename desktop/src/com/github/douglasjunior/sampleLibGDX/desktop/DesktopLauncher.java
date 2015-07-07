package com.github.douglasjunior.sampleLibGDX.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.github.douglasjunior.sampleLibGDX.MainApplication;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
            config.height = 720;
            config.width = 1024;
            config.backgroundFPS = 60;
            config.foregroundFPS = 60;
            config.stencil = 8;
		new LwjglApplication(new MainApplication(), config);
	}
}
