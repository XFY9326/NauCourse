package tool.xfy9326.naucourse.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.preference.PreferenceManager;

import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.snackbar.Snackbar;

import java.util.LinkedHashMap;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.asyncTasks.SchoolCalendarAsync;
import tool.xfy9326.naucourse.methods.BaseMethod;
import tool.xfy9326.naucourse.methods.DialogMethod;
import tool.xfy9326.naucourse.methods.NetMethod;

public class SchoolCalendarActivity extends AppCompatActivity {
    private int loadTime = 0;
    private LinkedHashMap<String, String> calendarList;
    private SharedPreferences sharedPreferences;
    private Bitmap calendarBitmap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_school_calendar);
        BaseMethod.getApp(this).setSchoolCalendarActivity(this);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        toolBarSet();
        getData();
        showAlert();
    }

    private void toolBarSet() {
        setSupportActionBar(findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_calendar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_calendar_share:
                String imageName = Config.SCHOOL_CALENDAR_IMAGE_FILE_NAME;
                String calendarName = sharedPreferences.getString(Config.PREFERENCE_SCHOOL_CALENDAR_NAME, null);
                if (calendarName != null) {
                    imageName = calendarName + ".jpeg";
                }
                DialogMethod.showImageShareDialog(this,
                        calendarBitmap,
                        imageName,
                        R.string.share_school_calendar,
                        R.string.school_calendar_share_error,
                        R.string.share_school_calendar);
                break;
            case R.id.menu_calendar_refresh:
                refresh();
                break;
            case R.id.menu_calendar_list:
                showSchoolCalendarList();
                break;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    private void refresh() {
        PhotoView photoView = findViewById(R.id.photoView_school_calendar);
        photoView.setVisibility(View.GONE);
        findViewById(R.id.layout_loading_school_calendar).setVisibility(View.VISIBLE);
        photoView.refreshDrawableState();
        getData();
    }

    private void showAlert() {
        if (sharedPreferences.getBoolean(Config.PREFERENCE_SCHOOL_CALENDAR_ENLARGE_ALERT, Config.DEFAULT_PREFERENCE_SCHOOL_CALENDAR_ENLARGE_ALERT)) {
            Snackbar.make(findViewById(R.id.layout_school_calendar_content), R.string.enlarge_alert, Snackbar.LENGTH_SHORT)
                    .setAction(R.string.no_alert_again, v -> sharedPreferences.edit().putBoolean(Config.PREFERENCE_SCHOOL_CALENDAR_ENLARGE_ALERT, false).apply())
                    .setActionTextColor(ResourcesCompat.getColor(getResources(), android.R.color.holo_red_light, getTheme()))
                    .show();
        }
    }

    private void showSchoolCalendarList() {
        if (calendarList != null && calendarList.size() > 0) {
            final String[] nameList = calendarList.keySet().toArray(new String[]{});
            AlertDialog.Builder builder = new AlertDialog.Builder(SchoolCalendarActivity.this);
            builder.setTitle(R.string.school_calendar_list);
            builder.setItems(nameList, (dialog, which) -> {
                String url = calendarList.get(nameList[which]);
                sharedPreferences.edit().putString(Config.PREFERENCE_SCHOOL_CALENDAR_NAME, nameList[which]).apply();
                sharedPreferences.edit().putString(Config.PREFERENCE_SCHOOL_CALENDAR_PAGE_URL, url).apply();
                refresh();
            });
            builder.setNeutralButton(R.string.school_calendar_default, (dialog, which) -> {
                sharedPreferences.edit().remove(Config.PREFERENCE_SCHOOL_CALENDAR_NAME).apply();
                sharedPreferences.edit().remove(Config.PREFERENCE_SCHOOL_CALENDAR_PAGE_URL).apply();
                refresh();
            });
            builder.setNegativeButton(android.R.string.cancel, null);
            builder.show();
        } else {
            Snackbar.make(findViewById(R.id.layout_school_calendar_content), R.string.school_calendar_list_empty, Snackbar.LENGTH_SHORT).show();
        }

    }

    synchronized public void setCalendarData(LinkedHashMap<String, String> calendarList, Bitmap bitmap) {
        setCalendarView(bitmap);
        if (bitmap != null && !bitmap.isRecycled()) {
            this.calendarBitmap = bitmap;
        }
        this.calendarList = calendarList;
    }

    synchronized private void setCalendarView(Bitmap bitmap) {
        if (bitmap != null) {
            PhotoView photoView = findViewById(R.id.photoView_school_calendar);
            findViewById(R.id.layout_loading_school_calendar).setVisibility(View.GONE);
            photoView.setVisibility(View.VISIBLE);
            photoView.setImageDrawable(new BitmapDrawable(getResources(), bitmap));
        } else if (loadTime > 1) {
            Snackbar.make(findViewById(R.id.layout_school_calendar_content), R.string.data_get_error, Snackbar.LENGTH_SHORT).show();
        }
    }

    synchronized public void lastViewSet(Context context) {
        //离线数据加载完成，开始拉取网络数据
        if (loadTime == 1 && NetMethod.isNetworkConnected(context) && BaseMethod.isDataAutoUpdate(context)) {
            getData();
        }
    }

    synchronized private void getData() {
        new SchoolCalendarAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, getApplicationContext());
    }

    public int getLoadTime() {
        return loadTime;
    }

    public void setLoadTime(int loadTime) {
        this.loadTime = loadTime;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (calendarBitmap != null && !calendarBitmap.isRecycled()) {
            calendarBitmap.recycle();
        }
        BaseMethod.getApp(this).setSchoolCalendarActivity(null);
        System.gc();
    }
}
