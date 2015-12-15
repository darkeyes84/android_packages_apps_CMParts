/**
 * Copyright (C) 2014 The Slimroms Project
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

package org.cyanogenmod.cmparts.input;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.cyanogenmod.cmparts.R;
import org.cyanogenmod.cmparts.SettingsPreferenceFragment;

public class NavBarDimensions extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String LIST_NAVIGATION_BAR_HEIGHT = "navigation_bar_height";
    private static final String LIST_NAVIGATION_BAR_WIDTH = "navigation_bar_width";

    ListPreference mNavigationBarHeight;
    ListPreference mNavigationBarWidth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.navbar_dimensions_settings);

        PreferenceScreen prefSet = getPreferenceScreen();

        // Height
        mNavigationBarHeight = (ListPreference) findPreference(LIST_NAVIGATION_BAR_HEIGHT);
        mNavigationBarHeight.setOnPreferenceChangeListener(this);

        boolean isPhone = TelephonyManager.getDefault().getCurrentPhoneType() != TelephonyManager.PHONE_TYPE_NONE;
        // Width
        mNavigationBarWidth = (ListPreference) findPreference(LIST_NAVIGATION_BAR_WIDTH);
        if (!isPhone) {
            prefSet.removePreference(mNavigationBarWidth);
            mNavigationBarWidth = null;
        } else {
            mNavigationBarWidth.setOnPreferenceChangeListener(this);
        }

        updateDimension();
    }

    private void updateDimension() {
        int navigationBarHeight = Settings.System.getInt(getContentResolver(),
                Settings.System.NAVIGATION_BAR_HEIGHT, -2);
        if (navigationBarHeight == -2) {
            navigationBarHeight =
                    (int) (getResources().getDimension(com.android.internal.R.dimen.navigation_bar_height)
                    / getResources().getDisplayMetrics().density);
        }
        mNavigationBarHeight.setValue(String.valueOf(navigationBarHeight));

        if (mNavigationBarWidth == null) {
            return;
        }
        int navigationBarWidth = Settings.System.getInt(getContentResolver(),
                Settings.System.NAVIGATION_BAR_WIDTH, -2);
        if (navigationBarWidth == -2) {
            navigationBarWidth =
                    (int) (getResources().getDimension(com.android.internal.R.dimen.navigation_bar_width)
                    / getResources().getDisplayMetrics().density);
        }
        mNavigationBarWidth.setValue(String.valueOf(navigationBarWidth));

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mNavigationBarHeight) {
            String newVal = (String) newValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.NAVIGATION_BAR_HEIGHT,
                    Integer.parseInt(newVal));
            return true;
        } else if (preference == mNavigationBarWidth) {
            String newVal = (String) newValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.NAVIGATION_BAR_WIDTH,
                    Integer.parseInt(newVal));
            return true;
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

}
