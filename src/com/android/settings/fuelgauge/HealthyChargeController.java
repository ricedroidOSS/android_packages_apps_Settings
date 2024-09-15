package com.android.settings.fuelgauge;

import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import androidx.preference.Preference;
import android.content.Context;
import android.provider.Settings;
import com.android.settings.core.BasePreferenceController;
import com.crdroid.settings.preferences.SystemSettingSeekBarPreference;
import com.android.settings.R;

public class HealthyChargeController extends BasePreferenceController implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "HealthyChargeController";
    private final String mMaxChargeLvlPath;

    public HealthyChargeController(Context context, String preferenceKey) {
        super(context, preferenceKey);
        String currMaxChargeLvlPath = SystemProperties.get("persist.sys.hct_path", "");
        String currMaxChargeLvlStartPath = SystemProperties.get("persist.sys.hct_start_path", "");
        mMaxChargeLvlPath = context.getResources().getString(R.string.config_healthy_charge_control_max_level_path);
        String mMaxChargeLvlStartPath = context.getResources().getString(R.string.config_healthy_charge_control_start_level_path);
        if (!TextUtils.equals(currMaxChargeLvlStartPath, mMaxChargeLvlStartPath)) {
            SystemProperties.set("persist.sys.hct_start_path", mMaxChargeLvlStartPath);
        }
        if (!TextUtils.equals(currMaxChargeLvlPath, mMaxChargeLvlPath)) {
            SystemProperties.set("persist.sys.hct_path", mMaxChargeLvlPath);
        }
        String currentPath = Settings.System.getString(
            mContext.getContentResolver(),
            "health_charge_threshold_path"
        );
        if (!TextUtils.equals(currentPath, mMaxChargeLvlPath)) {
            Settings.System.putString(mContext.getContentResolver(), "health_charge_threshold_path", mMaxChargeLvlPath);
        }
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void updateState(Preference preference) {
        if (preference instanceof SystemSettingSeekBarPreference) {
            SystemSettingSeekBarPreference seekBarPreference = (SystemSettingSeekBarPreference) preference;
            try {
                int currentChargeLevel = Settings.System.getInt(
                        mContext.getContentResolver(),
                        preference.getKey(),
                        100
                );
                seekBarPreference.setValue(currentChargeLevel);
            } catch (Exception e) {
                Log.e(TAG, "Error updating state for preference: " + preference.getKey(), e);
            }
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (newValue instanceof Integer) {
            int chargeStopLevel = (int) newValue;
            Settings.System.putInt(mContext.getContentResolver(), preference.getKey(), chargeStopLevel);
            SystemProperties.set("persist.sys.health_charge_threshold", String.valueOf(chargeStopLevel));
            SystemProperties.set("persist.sys.health_charge_threshold_start", String.valueOf(chargeStopLevel == 100 ? 0 : chargeStopLevel - 1));
            return true;
        }
        return false;
    }
}
