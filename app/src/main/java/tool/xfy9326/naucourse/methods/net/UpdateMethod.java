package tool.xfy9326.naucourse.methods.net;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import tool.xfy9326.naucourse.BuildConfig;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.tools.Updater;

public class UpdateMethod {
    private static boolean isCheckingUpdate = false;

    public static void checkUpdate(final Activity activity, final boolean manualCheck) {
        if (!isCheckingUpdate) {
            isCheckingUpdate = true;
            if (manualCheck) {
                Toast.makeText(activity, R.string.checking_update, Toast.LENGTH_SHORT).show();
            }
            if (NetMethod.isNetworkConnected(activity)) {
                Updater updater = Updater.getInstance();
                updater.checkUpdate(BuildConfig.VERSION_NAME, new Updater.OnUpdateListener() {
                    @Override
                    public void noUpdate() {
                        if (manualCheck && !activity.isDestroyed()) {
                            activity.runOnUiThread(() -> Toast.makeText(activity, activity.getString(R.string.no_update), Toast.LENGTH_SHORT).show());
                        }
                        isCheckingUpdate = false;
                    }

                    @Override
                    public void onError() {
                        if (manualCheck && !activity.isDestroyed()) {
                            activity.runOnUiThread(() -> Toast.makeText(activity, activity.getString(R.string.check_update_error), Toast.LENGTH_SHORT).show());
                        }
                        isCheckingUpdate = false;
                    }

                    @Override
                    public void findUpdate(String versionName, String updateInfo, final String updateUrl) {
                        if (!activity.isDestroyed()) {
                            LayoutInflater layoutInflater = activity.getLayoutInflater();
                            View view = layoutInflater.inflate(R.layout.dialog_application_update, activity.findViewById(R.id.layout_dialog_application_update));

                            TextView version = view.findViewById(R.id.textView_update_version);
                            version.setText(activity.getString(R.string.new_version, versionName));

                            TextView info = view.findViewById(R.id.textView_update_info);
                            info.setText(updateInfo.trim());

                            final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                            builder.setTitle(R.string.find_update);
                            builder.setCancelable(false);

                            builder.setPositiveButton(R.string.update_immediately, (dialog, which) -> {
                                Uri uri = Uri.parse(updateUrl);
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                activity.startActivity(intent);
                            });
                            builder.setNegativeButton(R.string.update_later, null);
                            builder.setView(view);
                            if (!activity.isDestroyed()) {
                                activity.runOnUiThread(() -> {
                                    try {
                                        Toast.makeText(activity, R.string.find_update, Toast.LENGTH_SHORT).show();
                                        builder.show();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                });
                            }
                        }
                        isCheckingUpdate = false;
                    }
                });
            } else {
                if (manualCheck && !activity.isDestroyed()) {
                    activity.runOnUiThread(() -> Toast.makeText(activity, activity.getString(R.string.network_error), Toast.LENGTH_SHORT).show());
                }
                isCheckingUpdate = false;
            }
        }
    }
}
