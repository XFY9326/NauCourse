package tool.xfy9326.naucourse.fragments;

import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import java.util.Objects;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.methods.VPNMethods;

public class GlobalSettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings_global);
    }

    @Override
    public void onResume() {
        super.onResume();
        PreferenceSet();
    }

    private void PreferenceSet() {
        ((Preference) Objects.requireNonNull(findPreference(Config.PREFERENCE_SCHOOL_VPN_MODE))).setOnPreferenceChangeListener((preference, newValue) -> {
            VPNMethods.setVPNMode(getActivity(), (boolean) newValue);
            return true;
        });

        ((Preference) Objects.requireNonNull(findPreference(Config.PREFERENCE_SCHOOL_VPN_SMART_MODE))).setOnPreferenceChangeListener((preference, newValue) -> {
            VPNMethods.setVPNSmartMode(getActivity(), (boolean) newValue);
            return true;
        });
    }
}
