package tool.xfy9326.naucourse.activities.async;

import android.content.Context;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import tool.xfy9326.naucourse.R;

@SuppressWarnings("unused")
abstract class BaseAsyncActivity extends AppCompatActivity {
    int loadTime = 0;

    void toolBarSet() {
        setSupportActionBar(findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public int getLoadTime() {
        return loadTime;
    }

    public void setLoadTime(int loadTime) {
        this.loadTime = loadTime;
    }

    protected abstract void getData();

    public abstract void lastViewSet(Context context);
}
