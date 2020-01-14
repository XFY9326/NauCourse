package tool.xfy9326.naucourse.asyncTasks;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import java.net.SocketTimeoutException;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.fragments.base.PersonFragment;
import tool.xfy9326.naucourse.methods.BaseMethod;
import tool.xfy9326.naucourse.methods.async.StudentCardMethod;
import tool.xfy9326.naucourse.methods.net.NetMethod;
import tool.xfy9326.naucourse.views.viewPagerAdapters.MainViewPagerAdapter;

public class CardMoneyAsync extends AsyncTask<Context, Void, Context> {
    private int cardLoadSuccess = -1;
    private int loadCode = Config.NET_WORK_GET_SUCCESS;
    @Nullable
    private String money;

    public CardMoneyAsync() {
        this.money = null;
    }

    @Override
    protected Context doInBackground(Context... context) {
        int loadTime = 0;
        MainViewPagerAdapter viewPagerAdapter = BaseMethod.getApp(context[0]).getViewPagerAdapter();
        if (viewPagerAdapter != null) {
            PersonFragment personFragment = viewPagerAdapter.getPersonFragment();
            try {
                if (personFragment != null) {
                    loadTime = personFragment.getLoadTime();
                }
                if (loadTime == 0) {
                    //首次只加载离线数据
                    money = PreferenceManager.getDefaultSharedPreferences(context[0]).getString(Config.PREFERENCE_STUDENT_CARD_MONEY, null);
                } else {
                    StudentCardMethod cardMethod = new StudentCardMethod(context[0]);
                    cardLoadSuccess = cardMethod.load();
                    if (cardLoadSuccess == Config.NET_WORK_GET_SUCCESS) {
                        money = cardMethod.getData(loadTime > 1);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (e instanceof SocketTimeoutException) {
                    cardLoadSuccess = Config.NET_WORK_ERROR_CODE_TIME_OUT;
                } else {
                    cardLoadSuccess = Config.NET_WORK_ERROR_CODE_CONNECT_ERROR;
                }
                loadCode = Config.NET_WORK_ERROR_CODE_CONNECT_ERROR;
            }
            if (personFragment != null) {
                personFragment.setLoadTime(++loadTime);
            }
            if (loadTime > 2) {
                NetMethod.showConnectErrorOnce = false;
            }
        }
        return context[0];
    }

    @Override
    protected void onPostExecute(@NonNull Context context) {
        MainViewPagerAdapter viewPagerAdapter = BaseMethod.getApp(context).getViewPagerAdapter();
        if (viewPagerAdapter != null) {
            PersonFragment personFragment = viewPagerAdapter.getPersonFragment();
            if (personFragment != null) {
                if (NetMethod.checkNetWorkCode(context, new int[]{cardLoadSuccess}, loadCode, false)) {
                    personFragment.moneyTextSet(money);
                }
                personFragment.lastViewSet(context);
            }
        }
        System.gc();
        super.onPostExecute(context);
    }
}
