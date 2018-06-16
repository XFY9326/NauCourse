package tool.xfy9326.naucourse.Activities;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import tool.xfy9326.naucourse.AsyncTasks.MoaAsync;
import tool.xfy9326.naucourse.Methods.BaseMethod;
import tool.xfy9326.naucourse.Methods.MoaMethod;
import tool.xfy9326.naucourse.Methods.NetMethod;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.Utils.Moa;
import tool.xfy9326.naucourse.Views.MoaAdapter;

public class MoaActivity extends AppCompatActivity {
    private SwipeRefreshLayout swipeRefreshLayout = null;
    private RecyclerView recyclerView = null;
    private MoaAdapter moaAdapter = null;
    private Moa moa = null;
    private int loadTime = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moa);
        BaseMethod.getApp(this).setMoaActivity(this);
        ToolBarSet();
        ViewSet();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_moa, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_moa_jump) {
            if (recyclerView != null && moa != null) {
                recyclerView.scrollToPosition(MoaMethod.getScrollPosition(moa));
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void ToolBarSet() {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void ViewSet() {
        ((TextView) findViewById(R.id.textView_moa_section_title)).setText(getString(R.string.moa_title, MoaMethod.MOA_PAST_SHOW_MONTH));

        recyclerView = findViewById(R.id.recyclerView_moa);
        recyclerView.setFocusableInTouchMode(false);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

        swipeRefreshLayout = findViewById(R.id.swipeLayout_moa);
        swipeRefreshLayout.setDistanceToTriggerSync(200);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (NetMethod.isNetworkConnected(MoaActivity.this)) {
                    getData();
                } else {
                    Snackbar.make(findViewById(R.id.layout_moa_content), R.string.network_error, Snackbar.LENGTH_SHORT).show();
                    swipeRefreshLayout.post(new Runnable() {
                        @Override
                        public void run() {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    });
                }
            }
        });
        if (loadTime == 0) {
            getData();
        }
    }

    synchronized private void getData() {
        new MoaAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, getApplicationContext());
    }

    public void setMoa(Moa moa) {
        if (moa != null) {
            if (moa.getCount() > 0) {
                this.moa = moa;
                if (moaAdapter == null) {
                    moaAdapter = new MoaAdapter(MoaActivity.this, moa);
                    recyclerView.setAdapter(moaAdapter);
                } else {
                    moaAdapter.updateMoa(moa);
                }
            } else {
                Snackbar.make(findViewById(R.id.layout_moa_content), R.string.moa_empty, Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    public int getLoadTime() {
        return loadTime;
    }

    public void setLoadTime(int loadTime) {
        this.loadTime = loadTime;
    }

    public void lastViewSet(Context context) {
        if (swipeRefreshLayout != null) {
            if (swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        }
        //离线数据加载完成，开始拉取网络数据
        if (loadTime == 1 && NetMethod.isNetworkConnected(context) && BaseMethod.isDataAutoUpdate(context)) {
            swipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(true);
                }
            });
            getData();
        }
    }

    @Override
    protected void onDestroy() {
        BaseMethod.getApp(this).setMoaActivity(null);
        super.onDestroy();
    }

}
