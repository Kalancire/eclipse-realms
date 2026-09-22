package com.eclipserealms.game.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.eclipserealms.game.EclipseRealmsGame;
import com.eclipserealms.game.util.DevicePerformanceTier;

/** Desktop entry point. Tuned to also run acceptably on old i3 3rd-gen laptops
 *  with integrated graphics (HD 4000-class). */
public class DesktopLauncher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Eclipse Realms");
        config.setWindowedMode(1280, 720);
        config.useVsync(true);
        // Cap FPS - an old i3 with integrated graphics gains nothing from
        // rendering faster than the display, and it just burns the CPU.
        config.setForegroundFPS(60);
        config.setIdleFPS(30);
        config.setResizable(true);

        boolean lowSpec = args.length > 0 && args[0].equalsIgnoreCase("--lowspec");
        DevicePerformanceTier.forceTier(lowSpec ? DevicePerformanceTier.Tier.LOW : null);

        new Lwjgl3Application(new EclipseRealmsGame(), config);
    }
}
