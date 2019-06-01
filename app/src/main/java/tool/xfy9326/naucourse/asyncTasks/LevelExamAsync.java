package tool.xfy9326.naucourse.asyncTasks;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.activities.LevelExamActivity;
import tool.xfy9326.naucourse.methods.BaseMethod;
import tool.xfy9326.naucourse.methods.DataMethod;
import tool.xfy9326.naucourse.methods.NetMethod;
import tool.xfy9326.naucourse.methods.netInfoMethods.LevelExamMethod;
import tool.xfy9326.naucourse.utils.LevelExam;

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
                    levelExam = levelExamMethod.getLevelExam(loadTime > 1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            loadCode = Config.NET_WORK_ERROR_CODE_CONNECT_ERROR;
        }
        if (levelExamActivity != null) {
            levelExamActivity.setLoadTime(++loadTime);
        }
        if (loadTime > 2) {
            BaseMethod.getApp(context[0]).setShowConnectErrorOnce(false);
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