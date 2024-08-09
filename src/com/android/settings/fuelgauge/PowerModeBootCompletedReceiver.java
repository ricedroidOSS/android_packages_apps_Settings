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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;

public class PowerModeBootCompletedReceiver extends BroadcastReceiver {

    private static final String TAG = "PowerModeBootCompletedReceiver";
    private static final String DEVICE_POWER_MODE_KEY = "device_power_mode";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "Boot completed - applying saved power mode");
            String powerMode = Settings.System.getString(context.getContentResolver(), DEVICE_POWER_MODE_KEY);
            if (powerMode != null) {
                applyPowerMode(context, powerMode);
            }
        }
    }

    private void applyPowerMode(Context context, String powerMode) {
        PowerModeController controller = new PowerModeController(context, "power_mode_preference");
        controller.applyPowerMode(powerMode);
    }
}
