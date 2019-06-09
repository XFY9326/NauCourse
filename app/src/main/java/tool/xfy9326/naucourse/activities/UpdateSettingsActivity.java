package tool.xfy9326.naucourse.activities;

import androidx.preference.PreferenceFragmentCompat;

import tool.xfy9326.naucourse.fragments.UpdateSettingsFragment;

public class UpdateSettingsActivity extends BaseSettingsActivity {
    @Override
    protected PreferenceFragmentCompat onCreateFragment() {
        return new UpdateSettingsFragment();
    }
}
