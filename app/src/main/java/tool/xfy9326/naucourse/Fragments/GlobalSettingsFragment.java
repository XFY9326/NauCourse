package tool.xfy9326.naucourse.Fragments;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Methods.UpdateMethod;
import tool.xfy9326.naucourse.R;

public class GlobalSettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
