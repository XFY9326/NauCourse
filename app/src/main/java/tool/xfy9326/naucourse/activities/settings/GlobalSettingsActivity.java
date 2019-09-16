package tool.xfy9326.naucourse.activities.settings;

import android.content.Intent;

import androidx.preference.PreferenceFragmentCompat;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.fragments.settings.GlobalSettingsFragment;

public class GlobalSettingsActivity extends BaseSettingsActivity {
    @Override
    protected PreferenceFragmentCompat onCreateFragment() {
        return new GlobalSettingsFragment();
    }

    public void closeApplication() {
        setResult(RESULT_OK, new Intent().putExtra(Config.INTENT_CLOSE_APPLICATION, true));
        finish();
    }
}
