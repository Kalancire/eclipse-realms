package com.eclipserealms.game.android;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.eclipserealms.game.EclipseRealmsGame;
import com.eclipserealms.game.util.DevicePerformanceTier;

/** Android entry point. Detects low-RAM / low-end devices (e.g. Snapdragon 680,
 *  8GB RAM phones) at launch and forces the game into its LOW performance tier
 *  before a single frame is rendered. */
public class AndroidLauncher extends AndroidApplication {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useAccelerometer = false;
        config.useCompass = false;
        config.useGyroscope = false;
        config.useWakelock = true;
        // Keep the default framebuffer format cheap on low-end GPUs
        config.r = 8; config.g = 8; config.b = 8; config.a = 0;
        config.numSamples = 0; // no MSAA - big win on Adreno 610/612 (SD680)

        detectAndApplyPerformanceTier();

        initialize(new EclipseRealmsGame(), config);
    }

    private void detectAndApplyPerformanceTier() {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        int cpuCores = Runtime.getRuntime().availableProcessors();
        long totalRamMb = 0;
        if (am != null) {
            am.getMemoryInfo(memInfo);
            totalRamMb = memInfo.totalMem / (1024 * 1024);
        }

        // 8GB-class phones with a mid-range SoC (e.g. SD680, 8 cores, ~7-8GB
        // reported RAM) should run on the LOW tier by default; the adaptive
        // manager can still step up if frame times allow it.
        if (totalRamMb > 0 && totalRamMb <= 8192 || cpuCores <= 8) {
            DevicePerformanceTier.forceTier(DevicePerformanceTier.Tier.LOW);
        } else {
            DevicePerformanceTier.forceTier(DevicePerformanceTier.Tier.MEDIUM);
        }
    }
}
