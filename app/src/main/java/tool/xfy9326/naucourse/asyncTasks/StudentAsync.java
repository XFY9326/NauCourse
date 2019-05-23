package tool.xfy9326.naucourse.asyncTasks;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.fragments.PersonFragment;
import tool.xfy9326.naucourse.methods.BaseMethod;
import tool.xfy9326.naucourse.methods.DataMethod;
import tool.xfy9326.naucourse.methods.NetMethod;
import tool.xfy9326.naucourse.methods.netInfoMethods.PersonMethod;
import tool.xfy9326.naucourse.utils.StudentInfo;
import tool.xfy9326.naucourse.utils.StudentLearnProcess;
import tool.xfy9326.naucourse.views.ViewPagerAdapter;

/**
 * Created by 10696 on 2018/3/2.
 */

public class StudentAsync extends AsyncTask<Context, Void, Context> {
    private int personLoadSuccess = -1;
    private int loadCode = Config.NET_WORK_GET_SUCCESS;
    @Nullable
    private StudentInfo studentInfo;
    @Nullable
    private StudentLearnProcess studentLearnProcess;

    public StudentAsync() {
        this.studentInfo = null;
        this.studentLearnProcess = null;
    }

    @Override
    protected Context doInBackground(Context... context) {
        int loadTime = 0;
        ViewPagerAdapter viewPagerAdapter = BaseMethod.getApp(context[0]).getViewPagerAdapter();
        if (viewPagerAdapter != null) {
            PersonFragment personFragment = viewPagerAdapter.getPersonFragment();
            try {
                if (personFragment != null) {
                    loadTime = personFragment.getLoadTime();
                }
                if (loadTime == 0) {
                    //首次只加载离线数据
                    studentInfo = (StudentInfo) DataMethod.getOfflineData(context[0], StudentInfo.class, PersonMethod.FILE_NAME_DATA, PersonMethod.IS_DATA_ENCRYPT);
                    studentLearnProcess = (StudentLearnProcess) DataMethod.getOfflineData(context[0], StudentLearnProcess.class, PersonMethod.FILE_NAME_PROCESS, PersonMethod.IS_PROCESS_ENCRYPT);
                    personLoadSuccess = Config.NET_WORK_GET_SUCCESS;
                } else {
                    PersonMethod personMethod = new PersonMethod(context[0]);
                    personLoadSuccess = personMethod.load();
                    if (personLoadSuccess == Config.NET_WORK_GET_SUCCESS) {
                        studentInfo = personMethod.getUserData(loadTime > 1);
                        studentLearnProcess = personMethod.getUserProcess(loadTime > 1);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                loadCode = Config.NET_WORK_ERROR_CODE_CONNECT_ERROR;
            }
            if (personFragment != null) {
                personFragment.setLoadTime(++loadTime);
            }
            if (loadTime > 2) {
                BaseMethod.getApp(context[0]).setShowConnectErrorOnce(false);
            }
        }
        return context[0];
    }

    @Override
    protected void onPostExecute(@NonNull Context context) {
        ViewPagerAdapter viewPagerAdapter = BaseMethod.getApp(context).getViewPagerAdapter();
        if (viewPagerAdapter != null) {
            PersonFragment personFragment = viewPagerAdapter.getPersonFragment();
            if (personFragment != null) {
                if (NetMethod.checkNetWorkCode(context, new int[]{personLoadSuccess}, loadCode, false)) {
                    personFragment.PersonViewSet(studentInfo, studentLearnProcess, context);
                }
                personFragment.lastViewSet(context, studentInfo == null || studentLearnProcess == null);
            }
        }
        System.gc();
        super.onPostExecute(context);
    }
}
