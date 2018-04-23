package tool.xfy9326.naucourse.Activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import lib.xfy9326.naujwc.NauJwcClient;
import tool.xfy9326.naucourse.AsyncTasks.InfoDetailAsync;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Methods.BaseMethod;
import tool.xfy9326.naucourse.Methods.JwInfoMethod;
import tool.xfy9326.naucourse.Methods.NetMethod;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.Utils.InfoDetail;
import tool.xfy9326.naucourse.Views.InfoAdapter;

/**
 * Created by 10696 on 2018/2/25.
 */

public class InfoDetailActivity extends AppCompatActivity {
    private String info_title;
    private String info_post;
    private String info_click;
    private String info_date;
    private String info_url;
    private String info_source;
    private String info_type;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_detail);
        BaseMethod.getApp(this).setInfoDetailActivity(this);
        if (getIntentData()) {
            ToolBarSet();
            ViewSet();
        } else {
            Toast.makeText(this, R.string.data_get_error, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void ToolBarSet() {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(info_type);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_info_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String url = null;
        if (info_source.equals(InfoAdapter.TOPIC_SOURCE_JW)) {
            url = JwInfoMethod.server_url + info_url;
        } else if (info_source.equals(InfoAdapter.TOPIC_SOURCE_JWC)) {
            url = NauJwcClient.server_url + info_url;
        }
        Intent intent = new Intent();
        if (url != null) {
            if (item.getItemId() == R.id.menu_info_detail_open_in_browser) {
                intent.setAction("android.intent.action.VIEW");
                Uri content_url = Uri.parse(url);
                intent.setData(content_url);
            } else if (item.getItemId() == R.id.menu_info_detail_share) {
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, info_type);
                intent.putExtra(Intent.EXTRA_TEXT, info_title + "\n" + url);
                intent = Intent.createChooser(intent, getString(R.string.share));
            }
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, R.string.no_available_application, Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        BaseMethod.getApp(this).setInfoDetailActivity(null);
        System.gc();
        super.onDestroy();
    }

    private boolean getIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            InfoDetail infoDetail = (InfoDetail) intent.getSerializableExtra(Config.INTENT_INFO_DETAIL);
            if (infoDetail != null) {
                info_url = infoDetail.getUrl();
                info_title = infoDetail.getTitle();
                info_post = infoDetail.getPost();
                info_click = infoDetail.getClick();
                info_date = infoDetail.getDate();
                info_source = infoDetail.getSource();
                info_type = infoDetail.getType();
                return true;
            }
        }
        return false;
    }

    private void ViewSet() {
        if (NetMethod.isNetworkConnected(this)) {
            getData();
        } else {
            Snackbar.make(findViewById(R.id.layout_info_detail_content), R.string.network_error, Snackbar.LENGTH_SHORT).show();
        }

        TextView textView_title = findViewById(R.id.textView_info_detail_title);
        TextView textView_post = findViewById(R.id.textView_info_detail_post);
        TextView textView_click = findViewById(R.id.textView_info_detail_click);
        TextView textView_date = findViewById(R.id.textView_info_detail_date);

        textView_title.setText(info_title);
        textView_post.setText(getString(R.string.info_post, info_post));
        if (info_click != null) {
            textView_click.setText(getString(R.string.info_click, info_click));
        } else {
            textView_click.setVisibility(View.GONE);
        }
        textView_date.setText(getString(R.string.info_date, info_date));
    }

    public void InfoDetailSet(String content) {
        if (content != null) {
            TextView textView_content = findViewById(R.id.textView_info_detail_content);
            if (info_source.equals(InfoAdapter.TOPIC_SOURCE_JWC)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    textView_content.setText(Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY));
                } else {
                    textView_content.setText(Html.fromHtml(content));
                }
            } else if (info_source.equals(InfoAdapter.TOPIC_SOURCE_JW)) {
                textView_content.setText(content);
            }

            textView_content.setVisibility(View.VISIBLE);
            ProgressBar progressBar_loading = findViewById(R.id.progressBar_info_detail_loading);
            progressBar_loading.setVisibility(View.GONE);
        }
    }

    private void getData() {
        InfoDetailAsync infoDetailAsync = new InfoDetailAsync();
        infoDetailAsync.setData(info_source, info_url);
        infoDetailAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, getApplicationContext());
    }

}