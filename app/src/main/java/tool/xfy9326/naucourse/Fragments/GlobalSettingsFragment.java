package tool.xfy9326.naucourse.Fragments;

import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Methods.UpdateMethod;
import tool.xfy9326.naucourse.R;

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
        findPreference(Config.PREFERENCE_UPDATE_NOW).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (isAdded()) {
                    UpdateMethod.checkUpdate(getActivity(), true);
                }
                return false;
            }
        });
    }
}
