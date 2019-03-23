package tool.xfy9326.naucourse.Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import tool.xfy9326.naucourse.AsyncTasks.SchoolCalendarAsync;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Methods.BaseMethod;
import tool.xfy9326.naucourse.Methods.ImageMethod;
import tool.xfy9326.naucourse.Methods.NetMethod;
import tool.xfy9326.naucourse.Methods.PermissionMethod;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.Tools.IO;

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_calendar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_calendar_share:
                shareCalendar();
                break;
            case R.id.menu_calendar_refresh:
                PhotoView photoView = findViewById(R.id.photoView_school_calendar);
                photoView.setVisibility(View.GONE);
                findViewById(R.id.layout_loading_school_calendar).setVisibility(View.VISIBLE);
                photoView.refreshDrawableState();
                getData();
                break;
        }
        return super.onOptionsItemSelected(item);
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

    synchronized public void setCalendarView(Bitmap bitmap) {
        if (bitmap != null) {
            PhotoView photoView = findViewById(R.id.photoView_school_calendar);
            findViewById(R.id.layout_loading_school_calendar).setVisibility(View.GONE);
            photoView.setVisibility(View.VISIBLE);
            photoView.setImageDrawable(new BitmapDrawable(getResources(), bitmap));
        } else if (loadTime > 1) {
            Snackbar.make(findViewById(R.id.layout_school_calendar_content), R.string.data_get_error, Snackbar.LENGTH_SHORT).show();
        }
    }

    private void shareCalendar() {
        if (PermissionMethod.checkStoragePermission(this, 0)) {
            try {
                final String path = Config.PICTURE_DICTIONARY_PATH + Config.SCHOOL_CALENDAR_FILE_NAME;
                if (IO.copyFile(ImageMethod.getSchoolCalendarImagePath(this), path, false)) {
                    final Uri imageUri = FileProvider.getUriForFile(this, Config.FILE_PROVIDER_AUTH, new File(path));

                    LayoutInflater layoutInflater = getLayoutInflater();
                    View view = layoutInflater.inflate(R.layout.dialog_share_image, (ViewGroup) findViewById(R.id.layout_dialog_share_image));
                    final PhotoView photoView = view.findViewById(R.id.photoView_share_image);
                    photoView.setImageURI(imageUri);

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setView(view);
                    builder.setTitle(R.string.share_school_calendar);
                    builder.setPositiveButton(R.string.share, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.setType("image/*");
                            intent.putExtra(Intent.EXTRA_STREAM, imageUri);
                            intent.addCategory(Intent.CATEGORY_DEFAULT);
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivity(Intent.createChooser(intent, getString(R.string.share_school_calendar)));
                        }
                    });
                    builder.setNegativeButton(android.R.string.cancel, null);
                    builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            photoView.refreshDrawableState();
                        }
                    });
                    builder.show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, R.string.school_calendar_share_error, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, R.string.permission_error, Toast.LENGTH_SHORT).show();
        }

    }

    synchronized public void lastViewSet(Context context) {
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
