package tool.xfy9326.naucourse.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.methods.BaseMethod;
import tool.xfy9326.naucourse.methods.DialogMethod;
import tool.xfy9326.naucourse.methods.NetMethod;
import tool.xfy9326.naucourse.methods.TempMethod;
import tool.xfy9326.naucourse.methods.UpdateMethod;
import tool.xfy9326.naucourse.views.FixedViewPager;
import tool.xfy9326.naucourse.views.MainViewPagerAdapter;

/**
 * Created by xfy9326 on 18-2-20.
 */

public class MainActivity extends AppCompatActivity {
    private final static String CURRENT_FRAGMENT_INDEX = "CURRENT_FRAGMENT_INDEX";
    private SharedPreferences sharedPreferences = null;
    private MainViewPagerAdapter viewPagerAdapter = null;
    private FixedViewPager viewPager = null;
    private boolean hasLogin = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        loginCheck();
        if (hasLogin) {
            setContentView(R.layout.activity_main);
            toolBarSet();
            if (savedInstanceState != null) {
                viewSet(savedInstanceState.getInt(CURRENT_FRAGMENT_INDEX, -1));
            } else {
                viewSet(-1);
            }
            tempLoad();
            BaseMethod.showNewVersionInfo(this, true);
            DialogMethod.showEULADialog(this, true, new BaseMethod.OnEULAListener() {
                @Override
                public void onAccept() {
                    sharedPreferences.edit().putBoolean(Config.PREFERENCE_EULA_ACCEPT, true).apply();
                }

                @Override
                public void onReject() {
                    Toast.makeText(MainActivity.this, R.string.eula_not_accept, Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCheck();
        netCheck();
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
        NetMethod.checkJwcAvailable(MainActivity.this);
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

    private void toolBarSet() {
        setSupportActionBar(findViewById(R.id.toolbar));
    }

    private void viewSet(int fragmentCurrentIndex) {
        viewPager = findViewById(R.id.viewPaper_main);
        final BottomNavigationView bnv = findViewById(R.id.bnv_main);
        if (viewPagerAdapter == null) {
            viewPagerAdapter = new MainViewPagerAdapter(getSupportFragmentManager());
        }

        viewPager.setOffscreenPageLimit(MainViewPagerAdapter.ITEM_COUNT);
        viewPager.setAdapter(viewPagerAdapter);
        if (getIntent() != null) {
            int position = getIntent().getIntExtra(Config.INTENT_VIEW_PAGER_POSITION, -1);
            if (position >= 0 && position <= MainViewPagerAdapter.ITEM_COUNT) {
                fragmentCurrentIndex = position;
            }
        }
        if (fragmentCurrentIndex < 0) {
            if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Config.PREFERENCE_DEFAULT_SHOW_TABLE_PAGE, Config.DEFAULT_PREFERENCE_DEFAULT_SHOW_TABLE_PAGE)) {
                viewPager.setCurrentItem(Config.VIEWPAGER_TABLE_PAGE, true);
                bnv.getMenu().getItem(Config.VIEWPAGER_TABLE_PAGE).setChecked(true);
            }
        } else {
            viewPager.setCurrentItem(fragmentCurrentIndex, true);
            bnv.getMenu().getItem(fragmentCurrentIndex).setChecked(true);
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

        bnv.setOnNavigationItemSelectedListener(item -> {
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
                default:
            }
            return false;
        });

        BaseMethod.getApp(this).setViewPagerAdapter(viewPagerAdapter);
    }

    //首次登陆提前加载考试与成绩的数据
    private void tempLoad() {
        if (getIntent() != null) {
            if (getIntent().getBooleanExtra(Config.INTENT_JUST_LOGIN, false)) {
                getIntent().removeExtra(Config.INTENT_JUST_LOGIN);
                Toast.makeText(this, R.string.login_success, Toast.LENGTH_SHORT).show();
                Toast.makeText(this, R.string.init_data_loading, Toast.LENGTH_SHORT).show();
                if (NetMethod.isNetworkConnected(this)) {
                    TempMethod.loadAllTemp(getApplicationContext());
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
