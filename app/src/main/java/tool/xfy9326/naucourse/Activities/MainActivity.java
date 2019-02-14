package tool.xfy9326.naucourse.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;
import tool.xfy9326.naucourse.AsyncTasks.TempAsync;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Methods.BaseMethod;
import tool.xfy9326.naucourse.Methods.NetMethod;
import tool.xfy9326.naucourse.Methods.UpdateMethod;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.Views.ViewPagerAdapter;

/**
 * Created by xfy9326 on 18-2-20.
 */

public class MainActivity extends AppCompatActivity {
    private final static String CURRENT_FRAGMENT_INDEX = "CURRENT_FRAGMENT_INDEX";
    private SharedPreferences sharedPreferences = null;
    private ViewPagerAdapter viewPagerAdapter = null;
    private ViewPager viewPager = null;
    private boolean hasLogin = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        loginCheck();
        if (hasLogin) {
            setContentView(R.layout.activity_main);
            ToolBarSet();
            updateCheck();
            netCheck();
            if (savedInstanceState != null) {
                ViewSet(savedInstanceState.getInt(CURRENT_FRAGMENT_INDEX, -1));
            } else {
                ViewSet(-1);
            }
            tempLoad();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(CURRENT_FRAGMENT_INDEX, viewPager.getCurrentItem());
    }

    @Override
    protected void onDestroy() {
        BaseMethod.getApp(this).setViewPagerAdapter(null);
        System.gc();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        BaseMethod.doubleClickExit(this);
    }

    private void netCheck() {
        NetMethod.isJwcAvailable(new NetMethod.OnAvailableListener() {
            @Override
            public void OnError() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, R.string.jwc_net_no_connection, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void loginCheck() {
        if (!sharedPreferences.getBoolean(Config.PREFERENCE_HAS_LOGIN, Config.DEFAULT_PREFERENCE_HAS_LOGIN)) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent, Config.REQUEST_ACTIVITY_LOGIN);
            finish();
        } else {
            if (!NetMethod.isNetworkConnected(this)) {
                Toast.makeText(this, R.string.network_error, Toast.LENGTH_SHORT).show();
            }
            hasLogin = true;
        }
    }

    synchronized private void updateCheck() {
        if (NetMethod.isNetworkConnected(this)) {
            if (sharedPreferences.getBoolean(Config.PREFERENCE_AUTO_CHECK_UPDATE, Config.DEFAULT_PREFERENCE_AUTO_CHECK_UPDATE)) {
                if (!sharedPreferences.getBoolean(Config.PREFERENCE_ONLY_UPDATE_APPLICATION_UNDER_WIFI, Config.DEFAULT_PREFERENCE_ONLY_UPDATE_APPLICATION_UNDER_WIFI) || NetMethod.isWifiNetWork(this)) {
                    UpdateMethod.checkUpdate(this, false);
                }
            }
        }
    }

    private void ToolBarSet() {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
    }

    private void ViewSet(int fragment_current_index) {
        viewPager = findViewById(R.id.viewPaper_main);
        final BottomNavigationView bnv = findViewById(R.id.bnv_main);
        if (viewPagerAdapter == null) {
            viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        }

        viewPager.setOffscreenPageLimit(ViewPagerAdapter.ITEM_COUNT);
        viewPager.setAdapter(viewPagerAdapter);
        if (fragment_current_index < 0) {
            if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Config.PREFERENCE_DEFAULT_SHOW_TABLE_PAGE, Config.DEFAULT_PREFERENCE_DEFAULT_SHOW_TABLE_PAGE)) {
                viewPager.setCurrentItem(Config.VIEWPAGER_TABLE_PAGE, true);
            }
        } else {
            viewPager.setCurrentItem(fragment_current_index, true);
        }

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                bnv.getMenu().getItem(position).setChecked(true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        bnv.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.bnv_item_home:
                        viewPager.setCurrentItem(0);
                        return true;
                    case R.id.bnv_item_table:
                        viewPager.setCurrentItem(1);
                        return true;
                    case R.id.bnv_item_person:
                        viewPager.setCurrentItem(2);
                        return true;
                }
                return false;
            }
        });

        BaseMethod.getApp(this).setViewPagerAdapter(viewPagerAdapter);
    }

    //首次登陆提前加载考试与成绩的数据
    private void tempLoad() {
        if (getIntent() != null) {
            if (getIntent().getBooleanExtra(Config.INTENT_JUST_LOGIN, false)) {
                Toast.makeText(this, R.string.login_success, Toast.LENGTH_SHORT).show();
                Toast.makeText(this, R.string.init_data_loading, Toast.LENGTH_SHORT).show();
                if (NetMethod.isNetworkConnected(this)) {
                    new TempAsync().execute(getApplicationContext());
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Config.REQUEST_ACTIVITY_LOGIN) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            if (!sharedPreferences.getBoolean(Config.PREFERENCE_HAS_LOGIN, Config.DEFAULT_PREFERENCE_HAS_LOGIN)) {
                finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
