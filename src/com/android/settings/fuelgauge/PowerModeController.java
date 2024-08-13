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
import android.content.IntentFilter;
import android.os.Handler;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import com.android.settings.core.BasePreferenceController;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PowerModeController extends BasePreferenceController implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "PowerModeController";
    private static final String CPU_POLICY_PATH = "/sys/devices/system/cpu/cpufreq/policy";
    private static final String SCALING_GOVERNOR = "scaling_governor";
    private static final String SCALING_AVAILABLE_GOVERNORS = "scaling_available_governors";
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
        List<String> modes = new ArrayList<>();
        if (getGovernorForMode("default") != null) {
            modes.add("default");
        }
        if (getGovernorForMode("conservative") != null) {
            modes.add("conservative");
        }
        if (getGovernorForMode("powersave") != null) {
            modes.add("powersave");
        }
        if (getGovernorForMode("performance") != null) {
            modes.add("performance");
        }
        if (getGovernorForMode("gameboost") != null) {
            modes.add("gameboost");
        }
        return modes;
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
        switch (mode) {
            case "default":
                return "Default";
            case "conservative":
                return "Conservative";
            case "powersave":
                return "Powersave";
            case "performance":
                return "Performance";
            case "gameboost":
                return "Game Boost";
            default:
                return mode;
        }
    }

    public void applyPowerMode(String powerMode) {
        int coreCount = Runtime.getRuntime().availableProcessors();
        for (int i = 0; i < coreCount; i++) {
            String policyPath = CPU_POLICY_PATH + i + "/";
            String availableGovernors = readFile(policyPath + SCALING_AVAILABLE_GOVERNORS);
            if (availableGovernors == null) {
                continue;
            }
            String governor = getGovernorForMode(powerMode);
            if (governor != null) {
                writeFile(policyPath + SCALING_GOVERNOR, governor);
                Log.d(TAG, "Applying governor: " + governor + " to core " + i);
            } else {
                Log.e(TAG, "No suitable governor found for power mode: " + powerMode);
            }
        }
        Settings.System.putString(mContext.getContentResolver(), DEVICE_POWER_MODE_KEY, powerMode);
        SystemProperties.set("persist.sys." + DEVICE_POWER_MODE_KEY, powerMode);
    }

    private String getGovernorForMode(String powerMode) {
        String availableGovernors = readFile(CPU_POLICY_PATH + "0/" + SCALING_AVAILABLE_GOVERNORS);
        if (availableGovernors == null) {
            Log.e(TAG, "Unable to read available governors.");
            return null;
        }
        if (availableGovernors == null) return null;
        String[] governors = availableGovernors.split(" ");
        switch (powerMode) {
            case "conservative":
                if (isGovernorAvailable(governors, "conservative")) {
                    return "conservative";
                } else {
                    return null;
                }
            case "powersave":
                if (isGovernorAvailable(governors, "powersave")) {
                    return "powersave";
                } else {
                    return null;
                }
            case "gameboost":
            case "performance":
                if (isGovernorAvailable(governors, "performance")) {
                    return "performance";
                } else {
                    return null;
                }
            case "default":
                if (isGovernorAvailable(governors, "sched_pixel")) {
                    return "sched_pixel";
                } else if (isGovernorAvailable(governors, "schedutil")) {
                    return "schedutil";
                } else {
                    for (String governor : governors) {
                        if (!governor.equals("powersave") && !governor.equals("conservative") &&
                            !governor.equals("performance")) {
                            return governor;
                        }
                    }
                }
        }
        return null;
    }

    private boolean isGovernorAvailable(String[] governors, String governor) {
        for (String availableGovernor : governors) {
            if (availableGovernor.equals(governor)) {
                return true;
            }
        }
        return false;
    }

    private String readFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        } catch (IOException e) {
            return null;
        }
        return result.toString();
    }

    private void writeFile(String path, String value) {
        try (FileWriter writer = new FileWriter(path)) {
            writer.write(value);
        } catch (IOException e) {
            Log.e(TAG, "Failed to write file: " + path, e);
        }
    }
}
