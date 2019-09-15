package tool.xfy9326.naucourse.activities.settings;

import androidx.preference.PreferenceFragmentCompat;

import tool.xfy9326.naucourse.fragments.settings.UpdateSettingsFragment;

public class UpdateSettingsActivity extends BaseSettingsActivity {
    @Override
    protected PreferenceFragmentCompat onCreateFragment() {
        return new UpdateSettingsFragment();
    }
}
