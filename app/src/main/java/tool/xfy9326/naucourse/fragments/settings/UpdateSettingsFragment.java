package tool.xfy9326.naucourse.fragments.settings;

import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import java.util.Objects;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.methods.net.NetMethod;
import tool.xfy9326.naucourse.methods.net.UpdateMethod;

public class UpdateSettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings_update);
    }

    @Override
    public void onResume() {
        super.onResume();
        preferenceSet();
    }

    @SuppressWarnings("SameReturnValue")
    private void preferenceSet() {
        ((Preference) Objects.requireNonNull(findPreference(Config.PREFERENCE_UPDATE_NOW))).setOnPreferenceClickListener(preference -> {
            if (isAdded()) {
                UpdateMethod.checkUpdate(getActivity(), true);
            }
            return false;
        });
        ((Preference) Objects.requireNonNull(findPreference(Config.PREFERENCE_WEAR_OS_SUPPORT_APP))).setOnPreferenceClickListener(preference -> {
            if (getActivity() != null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.attention);
                builder.setMessage(R.string.download_wear_os_support_app_attention);
                builder.setPositiveButton(android.R.string.ok, (dialog, which) -> NetMethod.viewUrlInBrowser(getActivity(), Config.WEAR_OS_SUPPORT_APP_DOWNLOAD_URL));
                builder.setNegativeButton(android.R.string.cancel, null);
                builder.show();
            }
            return false;
        });
    }
}
