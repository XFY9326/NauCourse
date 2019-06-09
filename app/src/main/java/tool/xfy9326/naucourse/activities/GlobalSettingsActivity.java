package tool.xfy9326.naucourse.activities;

import androidx.preference.PreferenceFragmentCompat;

import tool.xfy9326.naucourse.fragments.GlobalSettingsFragment;

public class GlobalSettingsActivity extends BaseSettingsActivity {
    @Override
    protected PreferenceFragmentCompat onCreateFragment() {
        return new GlobalSettingsFragment();
    }
}
