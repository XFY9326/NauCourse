package tool.xfy9326.naucourse.asyncTasks;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.net.SocketTimeoutException;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.activities.async.LevelExamActivity;
import tool.xfy9326.naucourse.beans.exam.LevelExam;
import tool.xfy9326.naucourse.methods.BaseMethod;
import tool.xfy9326.naucourse.methods.async.LevelExamMethod;
import tool.xfy9326.naucourse.methods.io.DataMethod;
import tool.xfy9326.naucourse.methods.net.NetMethod;

public class LevelExamAsync extends AsyncTask<Context, Void, Context> {
    private int levelExamLoadSuccess = -1;
    private int loadCode = Config.NET_WORK_GET_SUCCESS;
    @Nullable
    private LevelExam levelExam;

    public LevelExamAsync() {
        this.levelExam = null;
    }

    @Override
    protected Context doInBackground(Context... context) {
        int loadTime = 0;
        LevelExamActivity levelExamActivity = BaseMethod.getApp(context[0]).getLevelExamActivity();
        try {
            if (levelExamActivity != null) {
                loadTime = levelExamActivity.getLoadTime();
            }
            if (loadTime == 0) {
                //首次只加载离线数据
                levelExam = (LevelExam) DataMethod.getOfflineData(context[0], LevelExam.class, LevelExamMethod.FILE_NAME, LevelExamMethod.IS_ENCRYPT);
                levelExamLoadSuccess = Config.NET_WORK_GET_SUCCESS;
            } else {
                LevelExamMethod levelExamMethod = new LevelExamMethod(context[0]);
                levelExamLoadSuccess = levelExamMethod.load();
                if (levelExamLoadSuccess == Config.NET_WORK_GET_SUCCESS) {
                    levelExam = levelExamMethod.getData(loadTime > 1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (e instanceof SocketTimeoutException) {
                levelExamLoadSuccess = Config.NET_WORK_ERROR_CODE_TIME_OUT;
            } else {
                levelExamLoadSuccess = Config.NET_WORK_ERROR_CODE_CONNECT_ERROR;
            }
            loadCode = Config.NET_WORK_ERROR_CODE_CONNECT_ERROR;
        }
        if (levelExamActivity != null) {
            levelExamActivity.setLoadTime(++loadTime);
        }
        if (loadTime > 2) {
            NetMethod.showConnectErrorOnce = false;
        }
        return context[0];
    }

    @Override
    protected void onPostExecute(@NonNull Context context) {
        LevelExamActivity levelExamActivity = BaseMethod.getApp(context).getLevelExamActivity();
        if (levelExamActivity != null) {
            if (NetMethod.checkNetWorkCode(context, new int[]{levelExamLoadSuccess}, loadCode, false)) {
                levelExamActivity.setLevelExam(levelExam);
            }
            levelExamActivity.lastViewSet(context);
        }
        System.gc();
        super.onPostExecute(context);
    }
}
