/*******************************************************************************
 * Copyright 2012 Keith Johnson
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.ubergeek42.WeechatAndroid;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;

public class WeechatPreferencesActivity extends PreferenceActivity implements
        OnSharedPreferenceChangeListener {

    private SharedPreferences sharedPreferences;
    private EditTextPreference hostPref;
    private EditTextPreference portPref;
    private EditTextPreference textSizePref;
    private EditTextPreference timestampformatPref;
    private EditTextPreference passPref;
    private EditTextPreference stunnelCert;
    private EditTextPreference stunnelPass;
    private EditTextPreference sshHostPref;
    private EditTextPreference sshPortPref;
    private EditTextPreference sshPassPref;
    private EditTextPreference sshUserPref;
    private ListPreference prefixPref;
    private ListPreference connectionTypePref;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        sharedPreferences = getPreferenceScreen().getSharedPreferences();

        hostPref = (EditTextPreference) getPreferenceScreen().findPreference("host");
        portPref = (EditTextPreference) getPreferenceScreen().findPreference("port");
        passPref = (EditTextPreference) getPreferenceScreen().findPreference("password");
        textSizePref = (EditTextPreference) getPreferenceScreen().findPreference("text_size");
        timestampformatPref = (EditTextPreference) getPreferenceScreen().findPreference(
                "timestamp_format");
        stunnelCert = (EditTextPreference) getPreferenceScreen().findPreference("stunnel_cert");
        stunnelPass = (EditTextPreference) getPreferenceScreen().findPreference("stunnel_pass");

        sshHostPref = (EditTextPreference) getPreferenceScreen().findPreference("ssh_host");
        sshUserPref = (EditTextPreference) getPreferenceScreen().findPreference("ssh_user");
        sshPortPref = (EditTextPreference) getPreferenceScreen().findPreference("ssh_port");
        sshPassPref = (EditTextPreference) getPreferenceScreen().findPreference("ssh_pass");

        prefixPref = (ListPreference) getPreferenceScreen().findPreference("prefix_align");
        connectionTypePref = (ListPreference) getPreferenceScreen().findPreference(
                "connection_type");
        setTitle(R.string.preferences);
    }

    @Override
    protected void onPause() {
        super.onPause();

        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        hostPref.setSummary(sharedPreferences.getString("host", ""));
        portPref.setSummary(sharedPreferences.getString("port", "8001"));
        textSizePref.setSummary(sharedPreferences.getString("text_size", "10"));
        timestampformatPref.setSummary(sharedPreferences.getString("timestamp_format", "HH:mm:ss"));
        stunnelCert.setSummary(sharedPreferences.getString("stunnel_cert", "Not Set"));

        sshHostPref.setSummary(sharedPreferences.getString("ssh_host", ""));
        sshUserPref.setSummary(sharedPreferences.getString("ssh_user", ""));
        sshPortPref.setSummary(sharedPreferences.getString("ssh_port", "22"));

        prefixPref.setSummary(prefixPref.getEntry());
        connectionTypePref.setSummary(connectionTypePref.getEntry());

        String tmp;
        tmp = sharedPreferences.getString("password", null);
        if (tmp == null || tmp.equals("")) {
            passPref.setSummary("None Set");
        } else {
            passPref.setSummary("******");
        }
        tmp = sharedPreferences.getString("stunnel_pass", null);
        if (tmp == null || tmp.equals("")) {
            stunnelPass.setSummary("None Set");
        } else {
            stunnelPass.setSummary("******");
        }
        tmp = sharedPreferences.getString("ssh_pass", null);
        if (tmp == null || tmp.equals("")) {
            sshPassPref.setSummary("None Set");
        } else {
            sshPassPref.setSummary("******");
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("host")) {
            hostPref.setSummary(sharedPreferences.getString(key, ""));
        } else if (key.equals("port")) {
            portPref.setSummary(sharedPreferences.getString("port", "8001"));
        } else if (key.equals("password")) {
            String tmp = sharedPreferences.getString("password", null);
            if (tmp == null || tmp.equals("")) {
                passPref.setSummary("None Set");
            } else {
                passPref.setSummary("******");
            }
        } else if (key.equals("text_size")) {
            textSizePref.setSummary(sharedPreferences.getString("text_size", "10"));
        } else if (key.equals("timestamp_format")) {
            timestampformatPref.setSummary(sharedPreferences.getString("timestamp_format",
                    "HH:mm:ss"));
        } else if (key.equals("stunnel_cert")) {
            stunnelCert.setSummary(sharedPreferences.getString("stunnel_cert",
                    "/sdcard/weechat/client.p12"));
        } else if (key.equals("stunnel_pass")) {
            String tmp = sharedPreferences.getString("stunnel_pass", null);
            if (tmp == null || tmp.equals("")) {
                stunnelPass.setSummary("None Set");
            } else {
                stunnelPass.setSummary("******");
            }
        } else if (key.equals("ssh_host")) {
            sshHostPref.setSummary(sharedPreferences.getString(key, ""));
        } else if (key.equals("ssh_user")) {
            sshUserPref.setSummary(sharedPreferences.getString(key, ""));
        } else if (key.equals("port")) {
            sshPortPref.setSummary(sharedPreferences.getString(key, "22"));
        } else if (key.equals("ssh_pass")) {
            String tmp = sharedPreferences.getString("ssh_pass", null);
            if (tmp == null || tmp.equals("")) {
                sshPassPref.setSummary("None Set");
            } else {
                sshPassPref.setSummary("******");
            }
        } else if (key.equals("prefix_align")) {
            prefixPref.setSummary(prefixPref.getEntry());
        } else if (key.equals("connection_type")) {
            connectionTypePref.setSummary(connectionTypePref.getEntry());
        }
    }
}
