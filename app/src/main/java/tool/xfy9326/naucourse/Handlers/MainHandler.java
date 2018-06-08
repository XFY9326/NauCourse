package tool.xfy9326.naucourse.Handlers;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Fragments.TableFragment;
import tool.xfy9326.naucourse.Methods.BaseMethod;

/**
 * Created by 10696 on 2018/3/17.
 */

public class MainHandler extends Handler {
    private final Context context;

    public MainHandler(Context context) {
        this.context = context;
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        switch (msg.what) {
            case Config.HANDLER_RELOAD_TABLE:
                TableFragment tableFragment = BaseMethod.getApp(context).getViewPagerAdapter().getTableFragment();
                if (tableFragment != null) {
                    tableFragment.reloadTable(false);
                }
                break;
            case Config.HANDLER_RELOAD_TABLE_DATA:
                tableFragment = BaseMethod.getApp(context).getViewPagerAdapter().getTableFragment();
                if (tableFragment != null) {
                    tableFragment.reloadTable(true);
                }
                break;
        }
        super.handleMessage(msg);
    }
}
