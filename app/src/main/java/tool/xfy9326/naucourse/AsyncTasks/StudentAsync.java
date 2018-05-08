package tool.xfy9326.naucourse.AsyncTasks;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Fragments.PersonFragment;
import tool.xfy9326.naucourse.Methods.BaseMethod;
import tool.xfy9326.naucourse.Methods.DataMethod;
import tool.xfy9326.naucourse.Methods.NetMethod;
import tool.xfy9326.naucourse.Methods.PersonMethod;
import tool.xfy9326.naucourse.Utils.StudentInfo;
import tool.xfy9326.naucourse.Utils.StudentLearnProcess;

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
        try {
            int loadTime = 0;
            PersonFragment personFragment = BaseMethod.getApp(context[0]).getViewPagerAdapter().getPersonFragment();
            if (personFragment != null) {
                loadTime = personFragment.getLoadTime();
            }
            if (loadTime == 0) {
                //首次只加载离线数据
                studentInfo = (StudentInfo) DataMethod.getOfflineData(context[0], StudentInfo.class, PersonMethod.FILE_NAME_DATA);
                studentLearnProcess = (StudentLearnProcess) DataMethod.getOfflineData(context[0], StudentLearnProcess.class, PersonMethod.FILE_NAME_PROCESS);
                personLoadSuccess = Config.NET_WORK_GET_SUCCESS;
                loadTime++;
                if (personFragment != null) {
                    BaseMethod.getApp(context[0]).getViewPagerAdapter().getPersonFragment().setLoadTime(loadTime);
                }
            } else {
                PersonMethod personMethod = new PersonMethod(context[0]);
                personLoadSuccess = personMethod.load();
                if (personLoadSuccess == Config.NET_WORK_GET_SUCCESS) {
                    studentInfo = personMethod.getUserData(loadTime > 1);
                    studentLearnProcess = personMethod.getUserProcess(loadTime > 1);
                }

                loadTime++;
                personFragment.setLoadTime(loadTime);
            }
        } catch (Exception e) {
            e.printStackTrace();
            loadCode = Config.NET_WORK_ERROR_CODE_CONNECT_ERROR;
        }
        return context[0];
    }

    @Override
    protected void onPostExecute(@NonNull Context context) {
        PersonFragment personFragment = BaseMethod.getApp(context).getViewPagerAdapter().getPersonFragment();
        if (personFragment != null) {
            if (NetMethod.checkNetWorkCode(context, new int[]{personLoadSuccess}, loadCode)) {
                personFragment.PersonViewSet(studentInfo, studentLearnProcess, context);
            }
            personFragment.lastViewSet(context);
        }
        System.gc();
        super.onPostExecute(context);
    }
}
