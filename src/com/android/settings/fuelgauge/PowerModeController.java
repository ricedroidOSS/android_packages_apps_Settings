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
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.R;

import java.util.ArrayList;
import java.util.List;

public class PowerModeController extends BasePreferenceController implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "PowerModeController";
    private static final String DEVICE_POWER_MODE_KEY = "device_power_mode";
    
    private final List<String> availableModes;

    public PowerModeController(Context context, String preferenceKey) {
        super(context, preferenceKey);
        availableModes = initializeAvailableModes();
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void updateState(Preference preference) {
        ListPreference listPreference = (ListPreference) preference;
        setupPreferenceEntries(listPreference);
        String currentMode = Settings.System.getString(mContext.getContentResolver(), DEVICE_POWER_MODE_KEY);
        listPreference.setValue(currentMode);
    }
    
    private List<String> initializeAvailableModes() {
        String[] modesArray = mContext.getResources().getStringArray(R.array.available_power_modes);
        return new ArrayList<>(List.of(modesArray));
    }
    
    public List<String> getAvailableModes() {
        return availableModes;
    }

    private void setupPreferenceEntries(ListPreference listPreference) {
        List<String> entries = new ArrayList<>();
        List<String> entryValues = new ArrayList<>();
        for (String mode : availableModes) {
            entries.add(getModeLabel(mode));
            entryValues.add(mode);
        }
        listPreference.setEntries(entries.toArray(new CharSequence[0]));
        listPreference.setEntryValues(entryValues.toArray(new CharSequence[0]));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String powerMode = (String) newValue;
        applyPowerMode(powerMode);
        return true;
    }
    
    private String getModeLabel(String mode) {
        int resId;
        switch (mode) {
            case "default":
                resId = R.string.power_mode_default;
                break;
            case "conservative":
                resId = R.string.power_mode_conservative;
                break;
            case "powersave":
                resId = R.string.power_mode_powersave;
                break;
            case "performance":
                resId = R.string.power_mode_performance;
                break;
            case "gameboost":
                resId = R.string.power_mode_gameboost;
                break;
            default:
                return mode;
        }
        return mContext.getString(resId);
    }

    public void applyPowerMode(String powerMode) {
        Settings.System.putString(mContext.getContentResolver(), DEVICE_POWER_MODE_KEY, powerMode);
        SystemProperties.set("persist.sys." + DEVICE_POWER_MODE_KEY, powerMode);
    }
}
