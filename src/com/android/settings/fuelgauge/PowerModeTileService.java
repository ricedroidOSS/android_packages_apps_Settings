
/*
 * Copyright (C) 2023-2024 The RisingOS Android Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.settings.fuelgauge;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.provider.Settings;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.os.Handler;
import android.os.Looper;
import java.util.List;

import com.android.settings.R;

public class PowerModeTileService extends TileService {

    private static final String DEVICE_POWER_MODE_KEY = "device_power_mode";
    private PowerModeController mPowerModeController;
    private Mode currentMode;
    private ContentObserver settingsObserver;

    @Override
    public void onCreate() {
        super.onCreate();
        mPowerModeController = new PowerModeController(this, DEVICE_POWER_MODE_KEY);
        settingsObserver = new SettingsObserver(new Handler(Looper.getMainLooper()));
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        getContentResolver().registerContentObserver(
            Settings.System.getUriFor(DEVICE_POWER_MODE_KEY),
            false,
            settingsObserver
        );
        updateTileState();
    }

    @Override
    public void onStopListening() {
        super.onStopListening();
        getContentResolver().unregisterContentObserver(settingsObserver);
    }

    @Override
    public void onClick() {
        super.onClick();
        togglePowerMode();
        updateTileState();
    }

    private void updateTileState() {
        String modeString = Settings.System.getString(getContentResolver(), DEVICE_POWER_MODE_KEY);
        currentMode = Mode.fromString(modeString != null ? modeString : "default");
        getQsTile().setLabel(getString(R.string.power_mode_tile_label));
        getQsTile().setSubtitle(getString(currentMode.labelRes));
        getQsTile().setIcon(Icon.createWithResource(this, currentMode.iconRes));
        getQsTile().setState(currentMode == Mode.DEFAULT ? Tile.STATE_INACTIVE : Tile.STATE_ACTIVE);
        getQsTile().updateTile();
    }

    private void togglePowerMode() {
        List<String> availableModes = mPowerModeController.getAvailableModes();
        int currentIndex = availableModes.indexOf(currentMode.modeString);
        int nextIndex = (currentIndex + 1) % availableModes.size();
        String newModeString = availableModes.get(nextIndex);
        currentMode = Mode.fromString(newModeString);
        mPowerModeController.applyPowerMode(newModeString);
    }

    private class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            updateTileState();
        }
    }

    private enum Mode {
        DEFAULT("default", R.drawable.ic_power_default, R.string.power_mode_default),
        CONSERVATIVE("conservative", R.drawable.ic_battery_plus_24px, R.string.power_mode_conservative),
        POWERSAVE("powersave", R.drawable.ic_battery_plus_24px, R.string.power_mode_powersave),
        PERFORMANCE("performance", R.drawable.ic_performance_mode, R.string.power_mode_performance),
        GAMEBOOST("gameboost", R.drawable.ic_fire, R.string.power_mode_gameboost);

        final String modeString;
        final int iconRes;
        final int labelRes;

        Mode(String modeString, int iconRes, int labelRes) {
            this.modeString = modeString;
            this.iconRes = iconRes;
            this.labelRes = labelRes;
        }

        static Mode fromString(String modeString) {
            for (Mode mode : values()) {
                if (mode.modeString.equals(modeString)) {
                    return mode;
                }
            }
            return DEFAULT;
        }
    }
}
