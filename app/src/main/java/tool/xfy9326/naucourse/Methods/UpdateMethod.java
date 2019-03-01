package tool.xfy9326.naucourse.Methods;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import lib.xfy9326.updater.Updater;
import tool.xfy9326.naucourse.BuildConfig;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.R;

public class UpdateMethod {
    private static boolean isCheckingUpdate = false;

    public static void checkUpdate(final Activity activity, final boolean manualCheck) {
        if (!isCheckingUpdate) {
            isCheckingUpdate = true;
            if (manualCheck) {
                Toast.makeText(activity, R.string.checking_update, Toast.LENGTH_SHORT).show();
            }
            if (NetMethod.isNetworkConnected(activity)) {
                final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
                String type = sharedPreferences.getBoolean(Config.PREFERENCE_CHECK_BETA_UPDATE, Config.DEFAULT_PREFERENCE_CHECK_BETA_UPDATE) ? Updater.UPDATE_TYPE_BETA : Updater.UPDATE_TYPE_RELEASE;
                final boolean isImportantUpdate = sharedPreferences.getBoolean(Config.PREFERENCE_AUTO_CHECK_IMPORTANT_UPDATE, Config.DEFAULT_PREFERENCE_AUTO_CHECK_IMPORTANT_UPDATE);
                if (isImportantUpdate) {
                    type = Updater.UPDATE_TYPE_RELEASE;
                }
                Updater updater = new Updater(SecurityMethod.API_KEY, SecurityMethod.API_IV);
                updater.checkUpdate(BuildConfig.VERSION_CODE, Config.SUB_VERSION, type, new Updater.OnUpdateListener() {
                    @Override
                    public void noUpdate() {
                        if (manualCheck) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(activity, activity.getString(R.string.no_update), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        isCheckingUpdate = false;
                    }

                    @Override
                    public void onError() {
                        if (manualCheck) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(activity, activity.getString(R.string.check_update_error), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        isCheckingUpdate = false;
                    }

                    @Override
                    public void findUpdate(int versionCode, String versionName, int subVersion, String updateInfo, String updateType, final String updateUrl, boolean forceUpdate, String updateTime) {
                        if (!activity.isDestroyed() && (!isImportantUpdate || forceUpdate)) {
                            final String versionNew = versionName + "-" + subVersion + "(" + versionCode + ") " + updateType;
                            String lastCheckVersion = sharedPreferences.getString(Config.PREFERENCE_LAST_CHECK_VERSION, null);
                            if (manualCheck || (lastCheckVersion == null || !lastCheckVersion.equalsIgnoreCase(versionNew))) {
                                String versionNow = BuildConfig.VERSION_NAME + "-" + Config.SUB_VERSION + "(" + BuildConfig.VERSION_CODE + ") " + Config.VERSION_TYPE;
                                String showVersion = activity.getString(R.string.application_version) + "v" + versionNow + " -> v" + versionNew;
                                showVersion = showVersion.replace(Updater.UPDATE_TYPE_BETA, activity.getString(R.string.beta)).replace(Updater.UPDATE_TYPE_RELEASE, activity.getString(R.string.release)).replace(Config.DEBUG, activity.getString(R.string.debug));

                                LayoutInflater layoutInflater = activity.getLayoutInflater();
                                View view = layoutInflater.inflate(R.layout.dialog_application_update, (ViewGroup) activity.findViewById(R.id.layout_dialog_application_update));

                                TextView version = view.findViewById(R.id.textView_update_version);
                                version.setText(showVersion);

                                TextView time = view.findViewById(R.id.textView_update_time);
                                time.setText(activity.getString(R.string.update_time, updateTime));

                                TextView info = view.findViewById(R.id.textView_update_info);
                                info.setText(updateInfo.trim());

                                if (forceUpdate) {
                                    view.findViewById(R.id.textView_update_important_alert).setVisibility(View.VISIBLE);
                                }

                                final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                                builder.setTitle(R.string.find_update);

                                builder.setPositiveButton(R.string.update_immediately, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Uri uri = Uri.parse(updateUrl);
                                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                        activity.startActivity(intent);
                                    }
                                });
                                builder.setNegativeButton(R.string.update_later, null);
                                if (!forceUpdate) {
                                    builder.setNeutralButton(R.string.version_no_mention, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            sharedPreferences.edit().putString(Config.PREFERENCE_LAST_CHECK_VERSION, versionNew).apply();
                                        }
                                    });
                                }

                                builder.setView(view);
                                if (!activity.isDestroyed()) {
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                Toast.makeText(activity, R.string.find_update, Toast.LENGTH_SHORT).show();
                                                builder.show();
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                }
                            }
                        }
                        isCheckingUpdate = false;
                    }
                });
            } else {
                if (manualCheck) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity, activity.getString(R.string.network_error), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                isCheckingUpdate = false;
            }
        }
    }
}
