package tool.xfy9326.naucourse.Fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import tool.xfy9326.naucourse.Activities.MainActivity;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Methods.BaseMethod;
import tool.xfy9326.naucourse.Methods.LoginMethod;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.Receivers.UpdateReceiver;

/**
 * Created by xfy9326 on 18-2-20.
 */

public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_global);
        PreferenceSet();
    }

    private void PreferenceSet() {
        findPreference(Config.PREFERENCE_LOGIN_OUT).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (BaseMethod.isNetworkConnected(getActivity())) {
                    loginOut();
                } else {
                    Toast.makeText(getActivity(), R.string.network_error, Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });
        findPreference(Config.PREFERENCE_SHOW_NEXT_WEEK).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                return BaseMethod.getBaseApplication(getActivity()).getViewPagerAdapter().getTableFragment().reloadTable();
            }
        });
        findPreference(Config.PREFERENCE_SHOW_WEEKEND).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                return BaseMethod.getBaseApplication(getActivity()).getViewPagerAdapter().getTableFragment().reloadTable();
            }
        });
        findPreference(Config.PREFERENCE_NOTIFY_NEXT_CLASS).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if ((boolean) newValue) {
                    //初始化自动更新
                    getActivity().sendBroadcast(new Intent(UpdateReceiver.UPDATE_ACTION).putExtra(Config.INTENT_IS_ONLY_INIT, true));
                }
                return true;
            }
        });
    }

    private void loginOut() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String userId = sharedPreferences.getString(Config.PREFERENCE_USER_ID, Config.DEFAULT_PREFERENCE_USER_ID);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.login_out);
        builder.setMessage(getString(R.string.ask_login_out, userId));
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (!LoginMethod.loginOut(getActivity())) {
                                Looper.prepare();
                                Toast.makeText(getActivity(), R.string.login_out_error, Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            }
                        }
                    });
                    thread.start();
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                getActivity().startActivity(new Intent(getActivity(), MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));
                MainActivity activity = BaseMethod.getBaseApplication(getActivity()).getMainActivity();
                if (activity != null) {
                    activity.finish();
                }
                getActivity().finish();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.show();
    }
}
