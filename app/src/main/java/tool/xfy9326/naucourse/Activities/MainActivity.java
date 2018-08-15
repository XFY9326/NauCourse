package tool.xfy9326.naucourse.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

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
    private SharedPreferences sharedPreferences;
    private boolean hasLogin = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        loginCheck();
        if (hasLogin) {
            updateCheck();
            setContentView(R.layout.activity_main);
            ToolBarSet();
            ViewSet();
            tempLoad();
        }
    }

    @Override
    protected void onDestroy() {
        System.gc();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        BaseMethod.doubleClickExit(this);
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

    private void ViewSet() {
        ViewPager viewPager = findViewById(R.id.viewPaper_main);
        TabLayout tabLayout = findViewById(R.id.tabLayout_main);

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(viewPagerAdapter);
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Config.PREFERENCE_DEFAULT_SHOW_TABLE_PAGE, Config.DEFAULT_PREFERENCE_DEFAULT_SHOW_TABLE_PAGE)) {
            viewPager.setCurrentItem(Config.VIEWPAGER_TABLE_PAGE, true);
        }

        tabLayout.setupWithViewPager(viewPager);

        BaseMethod.getApp(this).setViewPagerAdapter(viewPagerAdapter);

        TabLayout.Tab tab_home = tabLayout.getTabAt(0);
        TabLayout.Tab tab_table = tabLayout.getTabAt(1);
        TabLayout.Tab tab_person = tabLayout.getTabAt(2);
        if (tab_home != null && tab_table != null && tab_person != null) {
            tab_home.setIcon(R.drawable.selector_tab_home);
            tab_table.setIcon(R.drawable.selector_tab_table);
            tab_person.setIcon(R.drawable.selector_tab_person);
        }
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
