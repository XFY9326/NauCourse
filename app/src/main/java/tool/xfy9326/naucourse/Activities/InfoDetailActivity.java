package tool.xfy9326.naucourse.Activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Methods.BaseMethod;
import tool.xfy9326.naucourse.Methods.InfoMethod;
import tool.xfy9326.naucourse.R;

/**
 * Created by 10696 on 2018/2/25.
 */

public class InfoDetailActivity extends AppCompatActivity {
    private String info_title;
    private String info_post;
    private String info_click;
    private String info_date;
    private String info_url;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_detail);
        ToolBarSet();
        GetData();
        ViewSet();
    }

    private void ToolBarSet() {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void GetData() {
        Intent intent = getIntent();
        if (intent != null) {
            info_url = intent.getStringExtra(Config.INTENT_INFO_DETAIL_URL);
            info_title = intent.getStringExtra(Config.INTENT_INFO_DETAIL_TITLE);
            info_post = intent.getStringExtra(Config.INTENT_INFO_DETAIL_POST);
            info_click = intent.getStringExtra(Config.INTENT_INFO_DETAIL_CLICK);
            info_date = intent.getStringExtra(Config.INTENT_INFO_DETAIL_DATE);
        }
    }

    private void ViewSet() {
        if (BaseMethod.isNetworkConnected(this)) {
            new InfoDetailAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, this);
        } else {
            Toast.makeText(this, R.string.network_error, Toast.LENGTH_SHORT).show();
        }

        TextView textView_title = findViewById(R.id.textView_info_detail_title);
        TextView textView_post = findViewById(R.id.textView_info_detail_post);
        TextView textView_click = findViewById(R.id.textView_info_detail_click);
        TextView textView_date = findViewById(R.id.textView_info_detail_date);

        textView_title.setText(info_title);
        textView_post.setText(getString(R.string.info_post, info_post));
        textView_click.setText(getString(R.string.info_click, info_click));
        textView_date.setText(getString(R.string.info_date, info_date));
    }

    private void InfoDetailSet(String content) {
        TextView textView_content = findViewById(R.id.textView_info_detail_content);
        textView_content.setText(content);
        ProgressBar progressBar_loading = findViewById(R.id.progressBar_info_detail_loading);
        progressBar_loading.setVisibility(View.GONE);
        textView_content.setVisibility(View.VISIBLE);
    }

    @SuppressLint("StaticFieldLeak")
    class InfoDetailAsync extends AsyncTask<Context, Void, String> {
        boolean loadSuccess = false;

        InfoDetailAsync() {
        }

        @Override
        protected String doInBackground(Context... context) {
            if (context[0] != null) {
                InfoMethod infoMethod = new InfoMethod(context[0]);
                loadSuccess = infoMethod.loadDetail(info_url);
                if (loadSuccess) {
                    return infoMethod.getDetail();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String str) {
            if (loadSuccess) {
                InfoDetailSet(str);
            }
            super.onPostExecute(str);
        }
    }
}