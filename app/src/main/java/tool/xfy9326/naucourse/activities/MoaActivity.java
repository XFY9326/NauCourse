package tool.xfy9326.naucourse.activities;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;

import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.asyncTasks.MoaAsync;
import tool.xfy9326.naucourse.methods.BaseMethod;
import tool.xfy9326.naucourse.methods.NetMethod;
import tool.xfy9326.naucourse.methods.netInfoMethods.MoaMethod;
import tool.xfy9326.naucourse.utils.Moa;
import tool.xfy9326.naucourse.views.recyclerAdapters.MoaAdapter;

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
        toolBarSet();
        viewSet();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_moa, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_moa_jump) {
            if (recyclerView != null && moa != null) {
                recyclerView.smoothScrollToPosition(MoaMethod.getScrollPosition(moa));
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void toolBarSet() {
        setSupportActionBar(findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void viewSet() {
        ((TextView) findViewById(R.id.textView_moa_section_title)).setText(getString(R.string.moa_title, MoaMethod.MOA_PAST_SHOW_MONTH));

        recyclerView = findViewById(R.id.recyclerView_moa);
        recyclerView.setFocusableInTouchMode(false);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        if (moaAdapter == null) {
            moaAdapter = new MoaAdapter(MoaActivity.this);
        }
        recyclerView.setAdapter(moaAdapter);

        swipeRefreshLayout = findViewById(R.id.swipeLayout_moa);
        swipeRefreshLayout.setDistanceToTriggerSync(200);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (NetMethod.isNetworkConnected(MoaActivity.this)) {
                getData();
            } else {
                Snackbar.make(findViewById(R.id.layout_moa_content), R.string.network_error, Snackbar.LENGTH_SHORT).show();
                swipeRefreshLayout.post(() -> swipeRefreshLayout.setRefreshing(false));
            }
        });

        findViewById(R.id.cardView_moa_title).setOnClickListener(v -> {
            if (recyclerView != null) {
                recyclerView.smoothScrollToPosition(0);
            }
        });
        if (loadTime == 0) {
            getData();
        }
    }

    synchronized private void getData() {
        BaseMethod.setRefreshing(swipeRefreshLayout, true);
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
                if (moaAdapter != null && moaAdapter.getItemCount() != 0) {
                    moaAdapter.clearAdapter();
                }
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
        //离线数据加载完成，开始拉取网络数据
        if (loadTime == 1 && NetMethod.isNetworkConnected(context) && BaseMethod.isDataAutoUpdate(context)) {
            getData();
        } else {
            BaseMethod.setRefreshing(swipeRefreshLayout, false);
        }
    }

    @Override
    protected void onDestroy() {
        BaseMethod.getApp(this).setMoaActivity(null);
        super.onDestroy();
    }

}
