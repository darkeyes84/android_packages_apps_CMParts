/*
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cyanogenmod.cmparts.networktraffic;

import android.annotation.Nullable;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.net.TrafficStats;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v14.preference.SwitchPreference;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import org.cyanogenmod.cmparts.PartsActivity;
import org.cyanogenmod.cmparts.SettingsPreferenceFragment;
import org.cyanogenmod.cmparts.R;
import org.cyanogenmod.cmparts.widget.SwitchBar;
import org.cyanogenmod.cmparts.widget.TrafficBarPreference;

public class NetworkTraffic extends SettingsPreferenceFragment implements 
    SwitchBar.OnSwitchChangeListener, Preference.OnPreferenceChangeListener {

    private static final String TAG = "NetworkTraffic";

    private static final String NETWORK_TRAFFIC_STATE = "network_traffic_state";
    private static final String NETWORK_TRAFFIC_UNIT = "network_traffic_unit";
    private static final String NETWORK_TRAFFIC_PERIOD = "network_traffic_period";
    private static final String NETWORK_TRAFFIC_AUTOHIDE = "network_traffic_autohide";
    private static final String NETWORK_TRAFFIC_HIDEARROW = "network_traffic_hidearrow";
    private static final String NETWORK_TRAFFIC_AUTOHIDE_THRESHOLD = "network_traffic_autohide_threshold";

    private int mNetTrafficVal;
    private int MASK_UP;
    private int MASK_DOWN;
    private int MASK_UNIT;
    private int MASK_PERIOD;

    private SwitchBar mSwitchBar;
    private ListPreference mNetTrafficState;
    private ListPreference mNetTrafficUnit;
    private ListPreference mNetTrafficPeriod;
    private SwitchPreference mNetTrafficAutohide;
    private SwitchPreference mNetTrafficHidearrow;
    private TrafficBarPreference mNetTrafficAutohideThreshold;

    private int oldSetting;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.network_traffic);

        loadResources();

        ContentResolver resolver = getActivity().getContentResolver();

        mNetTrafficState = (ListPreference) findPreference(NETWORK_TRAFFIC_STATE);
        mNetTrafficUnit = (ListPreference) findPreference(NETWORK_TRAFFIC_UNIT);
        mNetTrafficPeriod = (ListPreference) findPreference(NETWORK_TRAFFIC_PERIOD);

        mNetTrafficAutohide =
            (SwitchPreference) findPreference(NETWORK_TRAFFIC_AUTOHIDE);
        mNetTrafficAutohide.setChecked((Settings.System.getInt(getContentResolver(),
                Settings.System.NETWORK_TRAFFIC_AUTOHIDE, 0) == 1));
        mNetTrafficAutohide.setOnPreferenceChangeListener(this);

        mNetTrafficHidearrow =
            (SwitchPreference) findPreference(NETWORK_TRAFFIC_HIDEARROW);
        mNetTrafficHidearrow.setChecked((Settings.System.getInt(getContentResolver(),
                Settings.System.NETWORK_TRAFFIC_HIDEARROW, 0) == 1));
        mNetTrafficHidearrow.setOnPreferenceChangeListener(this);

        mNetTrafficAutohideThreshold = (TrafficBarPreference) findPreference(NETWORK_TRAFFIC_AUTOHIDE_THRESHOLD);
        int netTrafficAutohideThreshold = Settings.System.getInt(resolver,
                Settings.System.NETWORK_TRAFFIC_AUTOHIDE_THRESHOLD, 10);
        mNetTrafficAutohideThreshold.setValue(netTrafficAutohideThreshold / 1);
        mNetTrafficAutohideThreshold.setOnPreferenceChangeListener(this);

        // TrafficStats will return UNSUPPORTED if the device does not support it.
        if (TrafficStats.getTotalTxBytes() != TrafficStats.UNSUPPORTED &&
                TrafficStats.getTotalRxBytes() != TrafficStats.UNSUPPORTED) {
            mNetTrafficVal = Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.NETWORK_TRAFFIC_STATE, 0);
            oldSetting = Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.NETWORK_TRAFFIC_OLD_STATE, 3);
            int intIndex = mNetTrafficVal & (MASK_UP + MASK_DOWN);
            int oldIndex = oldSetting & (MASK_UP + MASK_DOWN);
            updateNetworkTrafficState();

            mNetTrafficState.setValueIndex(intIndex > 0 ? intIndex - 1 : oldIndex > 0 ? oldIndex - 1 : 2);
            mNetTrafficState.setSummary(mNetTrafficState.getEntry());
            mNetTrafficState.setOnPreferenceChangeListener(this);

            mNetTrafficUnit.setValueIndex(getBit(mNetTrafficVal, MASK_UNIT) ? 1 : 0);
            mNetTrafficUnit.setSummary(mNetTrafficUnit.getEntry());
            mNetTrafficUnit.setOnPreferenceChangeListener(this);

            intIndex = (mNetTrafficVal & MASK_PERIOD) >>> 16;
            intIndex = mNetTrafficPeriod.findIndexOfValue(String.valueOf(intIndex));
            mNetTrafficPeriod.setValueIndex(intIndex >= 0 ? intIndex : 1);
            mNetTrafficPeriod.setSummary(mNetTrafficPeriod.getEntry());
            mNetTrafficPeriod.setOnPreferenceChangeListener(this);
        }
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        boolean isChecked = (Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.NETWORK_TRAFFIC_STATE, 0) & (MASK_UP + MASK_DOWN)) != 0;
        mSwitchBar = ((PartsActivity) getActivity()).getSwitchBar();
        mSwitchBar.addOnSwitchChangeListener(this);
        mSwitchBar.setChecked(isChecked);
        mSwitchBar.show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mSwitchBar != null) {
			mSwitchBar.hide();
            mSwitchBar.removeOnSwitchChangeListener(this);
        }
    }

    @Override
    public void onSwitchChanged(Switch switchView, boolean isChecked) {
		if (!isChecked) {
			oldSetting = Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.NETWORK_TRAFFIC_STATE, 0);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NETWORK_TRAFFIC_OLD_STATE, oldSetting);
            mNetTrafficVal = setBit(mNetTrafficVal, MASK_UP, false);
            mNetTrafficVal = setBit(mNetTrafficVal, MASK_DOWN, false);
        } else {
			int intState = oldSetting & (MASK_UP + MASK_DOWN);
            mNetTrafficVal = setBit(mNetTrafficVal, MASK_UP, getBit(intState, MASK_UP));
            mNetTrafficVal = setBit(mNetTrafficVal, MASK_DOWN, getBit(intState, MASK_DOWN));
		}
        Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.NETWORK_TRAFFIC_STATE, isChecked ? oldSetting : mNetTrafficVal);
        updateNetworkTrafficState();
    }

    private void updateNetworkTrafficState() {
        boolean isChecked = (Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.NETWORK_TRAFFIC_STATE, 0) & (MASK_UP + MASK_DOWN)) != 0;
        if (isChecked) {
			mNetTrafficState.setEnabled(true);
            mNetTrafficUnit.setEnabled(true);
            mNetTrafficPeriod.setEnabled(true);
            mNetTrafficAutohide.setEnabled(true);
            mNetTrafficHidearrow.setEnabled(true);
        } else {
			mNetTrafficState.setEnabled(false);
            mNetTrafficUnit.setEnabled(false);
            mNetTrafficPeriod.setEnabled(false);
            mNetTrafficAutohide.setEnabled(false);
            mNetTrafficHidearrow.setEnabled(false);
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mNetTrafficState) {
            int intState = Integer.valueOf((String)newValue);
            mNetTrafficVal = setBit(mNetTrafficVal, MASK_UP, getBit(intState, MASK_UP));
            mNetTrafficVal = setBit(mNetTrafficVal, MASK_DOWN, getBit(intState, MASK_DOWN));
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NETWORK_TRAFFIC_STATE, mNetTrafficVal);
            int index = mNetTrafficState.findIndexOfValue((String) newValue);
            mNetTrafficState.setSummary(mNetTrafficState.getEntries()[index]);
            updateNetworkTrafficState();
            return true;
        } else if (preference == mNetTrafficUnit) {
            mNetTrafficVal = setBit(mNetTrafficVal, MASK_UNIT, ((String)newValue).equals("1"));
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NETWORK_TRAFFIC_STATE, mNetTrafficVal);
            int index = mNetTrafficUnit.findIndexOfValue((String) newValue);
            mNetTrafficUnit.setSummary(mNetTrafficUnit.getEntries()[index]);
            return true;
        } else if (preference == mNetTrafficPeriod) {
            int intState = Integer.valueOf((String)newValue);
            mNetTrafficVal = setBit(mNetTrafficVal, MASK_PERIOD, false) + (intState << 16);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NETWORK_TRAFFIC_STATE, mNetTrafficVal);
            int index = mNetTrafficPeriod.findIndexOfValue((String) newValue);
            mNetTrafficPeriod.setSummary(mNetTrafficPeriod.getEntries()[index]);
            return true;
        } else if (preference == mNetTrafficAutohide) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NETWORK_TRAFFIC_AUTOHIDE, value ? 1 : 0);
            return true;
        } else if (preference == mNetTrafficHidearrow) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NETWORK_TRAFFIC_HIDEARROW, value ? 1 : 0);
            return true;
        } else if (preference == mNetTrafficAutohideThreshold) {
            int threshold = (Integer) newValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NETWORK_TRAFFIC_AUTOHIDE_THRESHOLD, threshold * 1);
            return true;
        }
        return false;
    }

    private void loadResources() {
        Resources resources = getActivity().getResources();
        MASK_UP = resources.getInteger(R.integer.maskUp);
        MASK_DOWN = resources.getInteger(R.integer.maskDown);
        MASK_UNIT = resources.getInteger(R.integer.maskUnit);
        MASK_PERIOD = resources.getInteger(R.integer.maskPeriod);
    }

    private int setBit(int intNumber, int intMask, boolean blnState) {
        if (blnState) {
            return (intNumber | intMask);
        }
        return (intNumber & ~intMask);
    }

    private boolean getBit(int intNumber, int intMask) {
        return (intNumber & intMask) == intMask;
    }
}
