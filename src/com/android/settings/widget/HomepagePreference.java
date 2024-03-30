/*
 * Copyright (C) 2021 The Android Open Source Project
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

package com.android.settings.widget;

import android.content.Context;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.android.settings.R;
import com.android.settings.network.MobileNetworkPreferenceController;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;
import java.util.Set;

/** A customized layout for homepage preference. */
public class HomepagePreference extends Preference implements
        HomepagePreferenceLayoutHelper.HomepagePreferenceLayout {
        
    private final MobileNetworkPreferenceController mMobileNetworkPreferenceController;

    private final HomepagePreferenceLayoutHelper mHelper;
    private final Handler mHandler = new Handler();
    private PreferenceViewHolder mHolder;
    private final Runnable mConnectivityRunnable = new Runnable() {
        @Override
        public void run() {
            notifyChanges();
            mHandler.postDelayed(this, 2000);
        }
    };

    public HomepagePreference(Context context, AttributeSet attrs, int defStyleAttr,
                              int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mHelper = new HomepagePreferenceLayoutHelper(this);
        mMobileNetworkPreferenceController = new MobileNetworkPreferenceController(getContext());
    }

    public HomepagePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mHelper = new HomepagePreferenceLayoutHelper(this);
        mMobileNetworkPreferenceController = new MobileNetworkPreferenceController(getContext());
    }

    public HomepagePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mHelper = new HomepagePreferenceLayoutHelper(this);
        mMobileNetworkPreferenceController = new MobileNetworkPreferenceController(getContext());
    }

    public HomepagePreference(Context context) {
        super(context);
        mHelper = new HomepagePreferenceLayoutHelper(this);
        mMobileNetworkPreferenceController = new MobileNetworkPreferenceController(getContext());
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        mHelper.onBindViewHolder(holder);
        mHolder = holder;
        setPreferencesSummaryVisibility();
        mHandler.postDelayed(mConnectivityRunnable, 1000);
    }
    
    private void setPreferencesSummaryVisibility() {
        if (mHolder == null) return;
        String key = getKey();
        View summaryView = mHolder.findViewById(android.R.id.summary);
        if ("top_level_network".equals(key)|| "top_level_connected_devices".equals(key)) {
            summaryView.setVisibility(View.VISIBLE);
            setSummaryLayoutParams(summaryView, true);
        } else {
            summaryView.setVisibility(View.GONE);
            setSummaryLayoutParams(summaryView, false);
        }
    }

    private void notifyChanges() {
        if (mHolder == null) return;
        String key = getKey();
        if ("top_level_network".equals(key)) {
            String connectedNetwork = getConnectedNetwork(getContext());
            String summary = getContext().getString(mMobileNetworkPreferenceController.isAvailable() 
                ? R.string.network_dashboard_summary_mobile : R.string.network_dashboard_summary_no_mobile);
            setSummary(connectedNetwork != null ? connectedNetwork : summary);
        } else if ("top_level_connected_devices".equals(key)) {
            String connectedBluetooth = getConnectedBluetoothDevice(getContext());
            String summary = getContext().getString(R.string.connected_devices_dashboard_default_summary);
            setSummary(connectedBluetooth != null ? connectedBluetooth : summary);
        }
    }

    private String getConnectedNetwork(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                String ssid = wifiManager.getConnectionInfo().getSSID();
                if (ssid != null && ssid.startsWith("\"") && ssid.endsWith("\"")) {
                    ssid = ssid.substring(1, ssid.length() - 1);
                }
                return ssid;
            } else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                String networkOperatorName = telephonyManager.getNetworkOperatorName();
                int networkType = networkInfo.getSubtype();
                String type = "";
                switch (networkType) {
                    case TelephonyManager.NETWORK_TYPE_NR: // 5G
                        type = "5G";
                        break;
                    case TelephonyManager.NETWORK_TYPE_LTE: // 4G
                        type = "4G";
                        break;
                    case TelephonyManager.NETWORK_TYPE_HSPAP: // 3G
                        type = "3G";
                        break;
                    case TelephonyManager.NETWORK_TYPE_EDGE: // 2G
                        type = "2G";
                        break;
                    default:
                        type = "Data";
                        break;
                }
                return networkOperatorName + " - " + type;
            }
        }
        return null;
    }

    private String getConnectedBluetoothDevice(Context context) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            for (BluetoothDevice device : pairedDevices) {
                if (bluetoothAdapter.getProfileConnectionState(BluetoothAdapter.STATE_CONNECTED)
                        == BluetoothAdapter.STATE_CONNECTED) {
                    return device.getName();
                }
            }
        }
        return null;
    }

    private void setSummaryLayoutParams(View summaryView, boolean visible) {
        if (summaryView.getLayoutParams() != null) {
            summaryView.getLayoutParams().width = visible ? ViewGroup.LayoutParams.WRAP_CONTENT : 0;
            summaryView.getLayoutParams().height = visible ? ViewGroup.LayoutParams.WRAP_CONTENT : 0;
            summaryView.requestLayout();
        }
    }

    @Override
    public HomepagePreferenceLayoutHelper getHelper() {
        return mHelper;
    }

    @Override
    public void onDetached() {
        super.onDetached();
        mHandler.removeCallbacks(mConnectivityRunnable);
    }
}
