package tool.xfy9326.naucourse.methods;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;

import tool.xfy9326.naucourse.BuildConfig;
import tool.xfy9326.naucourse.R;

public class DialogBuilder {

    public static void showBuilderInMain(Activity context, final AlertDialog.Builder builder) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                builder.show();
            }
        });
    }

    public static AlertDialog.Builder buildNeedInstallSupportAppDialog(final Context context, final String nodeId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        builder.setMessage(R.string.support_app_not_installed);
        builder.setPositiveButton(R.string.go_to_install, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DeviceSupport.runInstallWebsite(context, nodeId);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        return builder;
    }

    public static AlertDialog.Builder buildOnlySupportAndroidDialog(final Activity context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        builder.setMessage(R.string.not_support_system);
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                context.finish();
            }
        });
        return builder;
    }

    public static AlertDialog.Builder buildSupportVersionCodeDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(context.getString(R.string.support_version_error, BuildConfig.SUPPORT_MIN_VERSION_NAME, BuildConfig.SUPPORT_MIN_SUB_VERSION, BuildConfig.SUPPORT_MIN_VERSION_CODE));
        builder.setPositiveButton(android.R.string.yes, null);
        return builder;
    }
}
