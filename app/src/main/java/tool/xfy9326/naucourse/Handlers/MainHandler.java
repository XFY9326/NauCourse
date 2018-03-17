package tool.xfy9326.naucourse.Handlers;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

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
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 0:
                TableFragment tableFragment = BaseMethod.getBaseApplication(context).getViewPagerAdapter().getTableFragment();
                tableFragment.reloadTable();
                break;
        }
        super.handleMessage(msg);
    }
}
