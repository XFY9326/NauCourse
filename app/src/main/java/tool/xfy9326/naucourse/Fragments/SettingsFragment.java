package tool.xfy9326.naucourse.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.widget.Toast;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Handlers.MainHandler;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.Receivers.UpdateReceiver;

/**
 * Created by xfy9326 on 18-2-20.
 */

public class SettingsFragment extends PreferenceFragment {
    private boolean updateCourseTable = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_global);
        PreferenceSet();
    }

    @Override
    public void onDestroy() {
        if (updateCourseTable) {
            MainHandler mainHandler = new MainHandler(getActivity());
            mainHandler.sendEmptyMessage(Config.HANDLER_RELOAD_TABLE);
        }
        super.onDestroy();
    }

    private void PreferenceSet() {
        Preference.OnPreferenceChangeListener tableReloadListener = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                updateCourseTable = true;
                return true;
            }
        };

        findPreference(Config.PREFERENCE_SHOW_NEXT_WEEK).setOnPreferenceChangeListener(tableReloadListener);
        findPreference(Config.PREFERENCE_SHOW_WEEKEND).setOnPreferenceChangeListener(tableReloadListener);
        findPreference(Config.PREFERENCE_SHOW_WIDE_TABLE).setOnPreferenceChangeListener(tableReloadListener);
        findPreference(Config.PREFERENCE_COURSE_TABLE_CELL_COLOR).setOnPreferenceChangeListener(tableReloadListener);

        findPreference(Config.PREFERENCE_NOTIFY_NEXT_CLASS).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if ((boolean) newValue) {
                    Toast.makeText(getActivity(), R.string.ask_lock_background, Toast.LENGTH_SHORT).show();
                    //初始化自动更新
                    getActivity().sendBroadcast(new Intent(UpdateReceiver.UPDATE_ACTION).putExtra(Config.INTENT_IS_ONLY_INIT, true));
                }
                return true;
            }
        });
    }

}
