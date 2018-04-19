package tool.xfy9326.naucourse.AsyncTasks;

import android.content.Context;
import android.os.AsyncTask;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Fragments.PersonFragment;
import tool.xfy9326.naucourse.Methods.BaseMethod;
import tool.xfy9326.naucourse.Methods.DataMethod;
import tool.xfy9326.naucourse.Methods.PersonMethod;
import tool.xfy9326.naucourse.Methods.SchoolTimeMethod;
import tool.xfy9326.naucourse.Utils.SchoolTime;
import tool.xfy9326.naucourse.Utils.StudentInfo;

/**
 * Created by 10696 on 2018/3/2.
 */

public class StudentAsync extends AsyncTask<Context, Void, Context> {
    private int personLoadSuccess = -1;
    private int timeLoadSuccess = -1;
    private int loadCode = Config.NET_WORK_GET_SUCCESS;
    private StudentInfo studentInfo;
    private SchoolTime schoolTime;

    public StudentAsync() {
        this.studentInfo = null;
        this.schoolTime = null;
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
                schoolTime = (SchoolTime) DataMethod.getOfflineData(context[0], SchoolTime.class, SchoolTimeMethod.FILE_NAME);
                personLoadSuccess = Config.NET_WORK_GET_SUCCESS;
                timeLoadSuccess = Config.NET_WORK_GET_SUCCESS;
                loadTime++;
                if (personFragment != null) {
                    BaseMethod.getApp(context[0]).getViewPagerAdapter().getPersonFragment().setLoadTime(loadTime);
                }
            } else {
                PersonMethod personMethod = new PersonMethod(context[0]);
                personLoadSuccess = personMethod.load();
                if (personLoadSuccess == Config.NET_WORK_GET_SUCCESS) {
                    studentInfo = personMethod.getUserData(loadTime > 1);
                }

                SchoolTimeMethod schoolTimeMethod = new SchoolTimeMethod(context[0]);
                timeLoadSuccess = schoolTimeMethod.load();
                if (timeLoadSuccess == Config.NET_WORK_GET_SUCCESS) {
                    schoolTime = schoolTimeMethod.getSchoolTime();
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
    protected void onPostExecute(Context context) {
        PersonFragment personFragment = BaseMethod.getApp(context).getViewPagerAdapter().getPersonFragment();
        if (personFragment != null) {
            if (BaseMethod.checkNetWorkCode(context, new int[]{personLoadSuccess, timeLoadSuccess}, loadCode)) {
                personFragment.PersonTextSet(studentInfo, context);
                personFragment.TimeTextSet(schoolTime, context);
            }
            personFragment.lastViewSet(context);
        }
        System.gc();
        super.onPostExecute(context);
    }
}
