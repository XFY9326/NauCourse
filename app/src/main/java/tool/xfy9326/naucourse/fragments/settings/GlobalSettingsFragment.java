package tool.xfy9326.naucourse.fragments.settings;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import java.util.Objects;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.methods.VPNMethods;

public class GlobalSettingsFragment extends PreferenceFragmentCompat {
    private int nightModeValue = 0;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings_global);
    }

    @Override
    public void onResume() {
        super.onResume();
        preferenceSet();
    }

    @SuppressWarnings("SameReturnValue")
    private void preferenceSet() {
        ((Preference) Objects.requireNonNull(findPreference(Config.PREFERENCE_NIGHT_MODE))).setOnPreferenceClickListener(preference -> {
            showNightModeChooseDialog();
            return true;
        });
        ((Preference) Objects.requireNonNull(findPreference(Config.PREFERENCE_SCHOOL_VPN_MODE))).setOnPreferenceChangeListener((preference, newValue) -> {
            VPNMethods.setVPNMode(getActivity(), (boolean) newValue);
            return true;
        });

        ((Preference) Objects.requireNonNull(findPreference(Config.PREFERENCE_SCHOOL_VPN_SMART_MODE))).setOnPreferenceChangeListener((preference, newValue) -> {
            VPNMethods.setVPNSmartMode(getActivity(), (boolean) newValue);
            return true;
        });
    }

    private void showNightModeChooseDialog() {
        if (isAdded() && getActivity() != null) {
            final int[] nightModeValueArr = getResources().getIntArray(R.array.night_mode_value);

            final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            final int mode = sharedPreferences.getInt(Config.PREFERENCE_NIGHT_MODE, Config.DEFAULT_PREFERENCE_NIGHT_MODE);
            nightModeValue = mode;

            int checkedIndex = 0;
            for (int i = 0; i < nightModeValueArr.length; i++) {
                if (nightModeValueArr[i] == mode) {
                    checkedIndex = i;
                    break;
                }
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.night_mode);
            builder.setSingleChoiceItems(R.array.night_mode, checkedIndex, (dialogInterface, i) -> nightModeValue = nightModeValueArr[i]);
            builder.setPositiveButton(android.R.string.yes, (dialogInterface, i) -> {
                if (nightModeValue != mode) {
                    sharedPreferences.edit().putInt(Config.PREFERENCE_NIGHT_MODE, nightModeValue).apply();
                    if (getActivity() != null) {
                        getActivity().getWindow().setWindowAnimations(android.R.style.Animation_Toast);
                        AppCompatDelegate.setDefaultNightMode(nightModeValue);
                    }
                }
            });
            builder.setNegativeButton(android.R.string.cancel, null);
            builder.show();
        }
    }
}
