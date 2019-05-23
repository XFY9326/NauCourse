package tool.xfy9326.naucourse.fragments;

import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import java.util.Objects;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.methods.UpdateMethod;

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
        ((Preference) Objects.requireNonNull(findPreference(Config.PREFERENCE_UPDATE_NOW))).setOnPreferenceClickListener(preference -> {
            if (isAdded()) {
                UpdateMethod.checkUpdate(getActivity(), true);
            }
            return false;
        });
    }
}
