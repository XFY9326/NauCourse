package tool.xfy9326.naucourse.Activities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.github.chrisbanes.photoview.PhotoView;

import tool.xfy9326.naucourse.AsyncTasks.SchoolCalendarAsync;
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
    }

    private void ToolBarSet() {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
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
        new SchoolCalendarAsync().executeOnExecutor(BaseMethod.getAsyncTaskExecutor(loadTime), getApplicationContext());
    }

    public int getLoadTime() {
        return loadTime;
    }

    public void setLoadTime(int loadTime) {
        this.loadTime = loadTime;
    }

    @Override
    protected void onDestroy() {
        PhotoView photoView = findViewById(R.id.photoView_school_calendar);
        if (photoView.getVisibility() == View.VISIBLE) {
            photoView.destroyDrawingCache();
        }
        BaseMethod.getApp(this).setSchoolCalendarActivity(null);
        super.onDestroy();
    }
}
