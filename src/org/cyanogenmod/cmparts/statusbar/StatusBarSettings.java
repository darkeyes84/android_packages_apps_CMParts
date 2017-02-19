/*
 * Copyright (C) 2014-2015 The CyanogenMod Project
 *               2017 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cyanogenmod.cmparts.statusbar;

import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.text.format.DateFormat;
import android.view.View;

import cyanogenmod.preference.CMSystemSettingListPreference;

import org.cyanogenmod.cmparts.R;
import org.cyanogenmod.cmparts.SettingsPreferenceFragment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class StatusBarSettings extends SettingsPreferenceFragment
        implements OnPreferenceChangeListener {

    private static final String STATUS_BAR_CLOCK_STYLE = "status_bar_clock";
    private static final String STATUS_BAR_AM_PM = "status_bar_am_pm";
    private static final String STATUS_BAR_BATTERY_STYLE = "status_bar_battery_style";
    private static final String STATUS_BAR_SHOW_BATTERY_PERCENT = "status_bar_show_battery_percent";
    private static final String STATUS_BAR_QUICK_QS_PULLDOWN = "qs_quick_pulldown";
    private static final String STATUS_BAR_SMART_PULLDOWN = "qs_smart_pulldown";
    private static final String STATUS_BAR_DATE = "status_bar_date";
    private static final String STATUS_BAR_DATE_STYLE = "status_bar_date_style";
    private static final String STATUS_BAR_DATE_FORMAT = "status_bar_date_format";
    private static final String STATUS_BAR_DATE_POSITION = "status_bar_date_position";

    private static final int STATUS_BAR_BATTERY_STYLE_HIDDEN = 4;
    private static final int STATUS_BAR_BATTERY_STYLE_TEXT = 6;
    private static final int PULLDOWN_DIR_NONE = 0;
    private static final int PULLDOWN_DIR_RIGHT = 1;
    private static final int PULLDOWN_DIR_LEFT = 2;
    public static final int STATUS_BAR_DATE_STYLE_LOWERCASE = 1;
    public static final int STATUS_BAR_DATE_STYLE_UPPERCASE = 2;

    private CMSystemSettingListPreference mQuickPulldown;
    private CMSystemSettingListPreference mSmartPulldown;
    private CMSystemSettingListPreference mStatusBarClock;
    private CMSystemSettingListPreference mStatusBarAmPm;
    private CMSystemSettingListPreference mStatusBarBattery;
    private CMSystemSettingListPreference mStatusBarBatteryShowPercent;

    private ListPreference mStatusBarDate;
    private ListPreference mStatusBarDateStyle;
    private ListPreference mStatusBarDateFormat;
    private ListPreference mStatusBarDatePosition;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.status_bar_settings);

        mStatusBarClock = (CMSystemSettingListPreference) findPreference(STATUS_BAR_CLOCK_STYLE);
        mStatusBarBatteryShowPercent =
                (CMSystemSettingListPreference) findPreference(STATUS_BAR_SHOW_BATTERY_PERCENT);

        mStatusBarAmPm = (CMSystemSettingListPreference) findPreference(STATUS_BAR_AM_PM);
        if (DateFormat.is24HourFormat(getActivity())) {
            mStatusBarAmPm.setEnabled(false);
            mStatusBarAmPm.setSummary(R.string.status_bar_am_pm_info);
        }

        mStatusBarBattery =
                (CMSystemSettingListPreference) findPreference(STATUS_BAR_BATTERY_STYLE);
        mStatusBarBattery.setOnPreferenceChangeListener(this);
        enableStatusBarBatteryDependents(mStatusBarBattery.getIntValue(2));

        mStatusBarDate = (ListPreference) findPreference(STATUS_BAR_DATE);
        mStatusBarDate.setOnPreferenceChangeListener(this);
        mStatusBarDate.setValue(Integer.toString(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.STATUS_BAR_DATE, 0)));
        mStatusBarDate.setSummary(mStatusBarDate.getEntry());

        mStatusBarDatePosition = (ListPreference) findPreference(STATUS_BAR_DATE_POSITION);
        mStatusBarDatePosition.setOnPreferenceChangeListener(this);
        mStatusBarDatePosition.setValue(Integer.toString(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.STATUS_BAR_DATE_POSITION, 0)));
        mStatusBarDatePosition.setSummary(mStatusBarDatePosition.getEntry());

        mStatusBarDateStyle = (ListPreference) findPreference(STATUS_BAR_DATE_STYLE);
        mStatusBarDateStyle.setOnPreferenceChangeListener(this);
        mStatusBarDateStyle.setValue(Integer.toString(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.STATUS_BAR_DATE_STYLE, 0)));
        mStatusBarDateStyle.setSummary(mStatusBarDateStyle.getEntry());

        mStatusBarDateFormat = (ListPreference) findPreference(STATUS_BAR_DATE_FORMAT);
        mStatusBarDateFormat.setOnPreferenceChangeListener(this);
        String dateFormat = Settings.System.getString(getActivity().getContentResolver(), 
                Settings.System.STATUS_BAR_DATE_FORMAT);
        if (dateFormat == null) {
            dateFormat = "EEE";
        }
        mStatusBarDateFormat.setValue(dateFormat);
        mStatusBarDateFormat.setSummary(DateFormat.format(dateFormat, new Date()));

        parseClockDateFormats();

        boolean mStatusBarDateToggle = Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_DATE, 0) != 0;
        if (!mStatusBarDateToggle) {
            mStatusBarDateStyle.setEnabled(false);
            mStatusBarDatePosition.setEnabled(false);
            mStatusBarDateFormat.setEnabled(false);
        }

        mQuickPulldown =
                (CMSystemSettingListPreference) findPreference(STATUS_BAR_QUICK_QS_PULLDOWN);
        mQuickPulldown.setOnPreferenceChangeListener(this);
        mSmartPulldown =
                (CMSystemSettingListPreference) findPreference(STATUS_BAR_SMART_PULLDOWN);
        mSmartPulldown.setOnPreferenceChangeListener(this);
        updateQuickPulldownSummary(mQuickPulldown.getIntValue(0), mSmartPulldown.getIntValue(0));
    }

    @Override
    public void onResume() {
        super.onResume();

        // Adjust status bar preferences for RTL
        if (getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            mStatusBarClock.setEntries(R.array.status_bar_clock_position_entries_rtl);
            mQuickPulldown.setEntries(R.array.status_bar_quick_qs_pulldown_entries_rtl);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mQuickPulldown) {
			int value = Integer.parseInt((String) newValue);
            updateQuickPulldownSummary(value, mSmartPulldown.getIntValue(0));
        } else if (preference == mSmartPulldown) {
			int value = Integer.parseInt((String) newValue);
			updateQuickPulldownSummary(mQuickPulldown.getIntValue(0), value);
        } else if (preference == mStatusBarBattery) {
			int value = Integer.parseInt((String) newValue);
            enableStatusBarBatteryDependents(value);
        } else if (preference == mStatusBarDate) {
			int value = Integer.parseInt((String) newValue);
            int index = mStatusBarDate.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_DATE, value);
            mStatusBarDate.setSummary(mStatusBarDate.getEntries()[index]);
            if (value == 0) {
                mStatusBarDateStyle.setEnabled(false);
                mStatusBarDatePosition.setEnabled(false);
                mStatusBarDateFormat.setEnabled(false);
            } else {
                mStatusBarDateStyle.setEnabled(true);
                mStatusBarDatePosition.setEnabled(true);
                mStatusBarDateFormat.setEnabled(true);
            }
        } else if (preference == mStatusBarDatePosition) {
			int value = Integer.parseInt((String) newValue);
            int index = mStatusBarDatePosition.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_DATE_POSITION, value);
            mStatusBarDatePosition.setSummary(mStatusBarDatePosition.getEntries()[index]);
            parseClockDateFormats();
        } else if (preference == mStatusBarDateStyle) {
			int value = Integer.parseInt((String) newValue);
            int index = mStatusBarDateStyle.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_DATE_STYLE, value);
            mStatusBarDateStyle.setSummary(mStatusBarDateStyle.getEntries()[index]);
            parseClockDateFormats();
        } else if (preference == mStatusBarDateFormat) {
            if ((String) newValue != null) {
                Settings.System.putString(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_DATE_FORMAT, (String) newValue);
                mStatusBarDateFormat.setSummary(
                    DateFormat.format((String) newValue, new Date()));
            }
        }
        return true;
    }

    private void enableStatusBarBatteryDependents(int batteryIconStyle) {
        mStatusBarBatteryShowPercent.setEnabled(
                batteryIconStyle != STATUS_BAR_BATTERY_STYLE_HIDDEN
                && batteryIconStyle != STATUS_BAR_BATTERY_STYLE_TEXT);
    }

    private void updateQuickPulldownSummary(int quickvalue, int smartvalue) {
        String summary="";
        switch (quickvalue) {
            case PULLDOWN_DIR_NONE:
                summary = getResources().getString(
                    R.string.status_bar_quick_qs_pulldown_off);
                break;

            case PULLDOWN_DIR_LEFT:
            case PULLDOWN_DIR_RIGHT:
                summary = getResources().getString(
                    R.string.status_bar_quick_qs_pulldown_summary,
                    getResources().getString(quickvalue == PULLDOWN_DIR_LEFT
                        ? R.string.status_bar_quick_qs_pulldown_summary_left
                        : R.string.status_bar_quick_qs_pulldown_summary_right));
                break;
        }
        mQuickPulldown.setSummary(summary);

        if (smartvalue == 0) {
            mSmartPulldown.setSummary(R.string.smart_pulldown_off);
        } else {
            mSmartPulldown.setSummary(smartvalue == 3
                    ? R.string.smart_pulldown_none_summary
                    : R.string.smart_pulldown_summary);
        }
    }

    private void parseClockDateFormats() {
        // Parse and repopulate mStatusBarDateFormats's entries based on current date.
        String[] dateEntries = getResources().getStringArray(R.array.status_bar_date_format_entries_values);
        CharSequence parsedDateEntries[];
        parsedDateEntries = new String[dateEntries.length];
        Date now = new Date();

        int dateFormat = Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.STATUS_BAR_DATE_STYLE, 0);
        for (int i = 0; i < dateEntries.length; i++) {
            String newDate;
            CharSequence dateString = DateFormat.format(dateEntries[i], now);
            if (dateFormat == STATUS_BAR_DATE_STYLE_LOWERCASE) {
                newDate = dateString.toString().toLowerCase();
            } else if (dateFormat == STATUS_BAR_DATE_STYLE_UPPERCASE) {
                newDate = dateString.toString().toUpperCase();
            } else {
                newDate = dateString.toString();
            }
            parsedDateEntries[i] = newDate;
        }
        mStatusBarDateFormat.setEntries(parsedDateEntries);
    }
}
