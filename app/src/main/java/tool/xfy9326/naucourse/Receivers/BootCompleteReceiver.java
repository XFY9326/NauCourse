package tool.xfy9326.naucourse.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Widget.NextClassWidget;

public class BootCompleteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Config.PREFERENCE_NOTIFY_NEXT_CLASS, Config.DEFAULT_PREFERENCE_NOTIFY_NEXT_CLASS)) {
                context.sendBroadcast(new Intent(context, CourseUpdateReceiver.class).setAction(CourseUpdateReceiver.UPDATE_ACTION).setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES).putExtra(Config.INTENT_IS_ONLY_INIT, true));
            }
            context.sendBroadcast(new Intent(NextClassWidget.ACTION_ON_CLICK));
        }
    }
}
