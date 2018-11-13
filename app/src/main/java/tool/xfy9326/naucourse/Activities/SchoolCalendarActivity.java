package tool.xfy9326.naucourse.Activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;

import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import tool.xfy9326.naucourse.AsyncTasks.SchoolCalendarAsync;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Methods.BaseMethod;
import tool.xfy9326.naucourse.Methods.NetMethod;
import tool.xfy9326.naucourse.R;

public class SchoolCalendarActivity extends AppCompatActivity {
    private int loadTime = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_school_calendar);
        BaseMethod.getApp(this).setSchoolCalendarActivity(this);
        ToolBarSet();
        getData();
        showAlert();
    }

    private void ToolBarSet() {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void showAlert() {
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences.getBoolean(Config.PREFERENCE_SCHOOL_CALENDAR_ENLARGE_ALERT, Config.DEFAULT_PREFERENCE_SCHOOL_CALENDAR_ENLARGE_ALERT)) {
            Snackbar.make(findViewById(R.id.layout_school_calendar_content), R.string.enlarge_alert, Snackbar.LENGTH_SHORT).setAction(R.string.no_alert_again, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sharedPreferences.edit().putBoolean(Config.PREFERENCE_SCHOOL_CALENDAR_ENLARGE_ALERT, false).apply();
                }
            }).setActionTextColor(getResources().getColor(android.R.color.holo_red_light)).show();
        }
    }

    public void setCalendarView(Bitmap bitmap) {
        if (bitmap != null) {
            PhotoView photoView = findViewById(R.id.photoView_school_calendar);
            findViewById(R.id.layout_loading_school_calendar).setVisibility(View.GONE);
            photoView.setVisibility(View.VISIBLE);
            photoView.setImageDrawable(new BitmapDrawable(getResources(), bitmap));
        } else if (loadTime > 1) {
            Snackbar.make(findViewById(R.id.layout_school_calendar_content), R.string.data_get_error, Snackbar.LENGTH_SHORT).show();
        }
    }

    public void lastViewSet(Context context) {
        //离线数据加载完成，开始拉取网络数据
        if (loadTime == 1 && NetMethod.isNetworkConnected(context) && BaseMethod.isDataAutoUpdate(context)) {
            getData();
        }
    }

    synchronized private void getData() {
        if (loadTime == 0) {
            new SchoolCalendarAsync().execute(getApplicationContext());
        } else {
            new SchoolCalendarAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, getApplicationContext());
        }
    }

    public int getLoadTime() {
        return loadTime;
    }

    public void setLoadTime(int loadTime) {
        this.loadTime = loadTime;
    }

    @Override
    protected void onDestroy() {
        BaseMethod.getApp(this).setSchoolCalendarActivity(null);
        System.gc();
        super.onDestroy();
    }
}
