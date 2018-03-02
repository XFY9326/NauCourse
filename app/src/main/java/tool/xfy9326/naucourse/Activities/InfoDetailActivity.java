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
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Methods.BaseMethod;
import tool.xfy9326.naucourse.Methods.JwInfoMethod;
import tool.xfy9326.naucourse.Methods.JwcInfoMethod;
import tool.xfy9326.naucourse.R;
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
            info_source = intent.getStringExtra(Config.INTENT_INFO_DETAIL_SOURCE);
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
        if (info_click != null) {
            textView_click.setText(getString(R.string.info_click, info_click));
        } else {
            textView_click.setVisibility(View.GONE);
        }
        textView_date.setText(getString(R.string.info_date, info_date));
    }

    private void InfoDetailSet(String content, String[] extraFile) {
        if (content != null) {
            TextView textView_content = findViewById(R.id.textView_info_detail_content);
            if (extraFile != null) {
                if (extraFile.length > 0) {
                    int p = 0;
                    int[] strLength = new int[extraFile.length];
                    int[] strStart = new int[extraFile.length];
                    Pattern pattern = Pattern.compile("(附件).*(\\.\\S*)");
                    Matcher matcher = pattern.matcher(content);
                    while (matcher.find()) {
                        String text = matcher.group();
                        content = content.replace(text, "\n" + text);
                        strLength[p] = text.length();
                        strStart[p] = content.indexOf(text);
                        p++;
                    }

                    SpannableString spannableString = new SpannableString(content);
                    for (int i = 0; i < extraFile.length; i++) {
                        spannableString.setSpan(new URLSpan(extraFile[i]), strStart[i], strStart[i] + strLength[i], Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    textView_content.setText(spannableString);
                    textView_content.setMovementMethod(LinkMovementMethod.getInstance());
                } else {
                    textView_content.setText(content);
                }
            } else {
                textView_content.setText(content);
            }
            textView_content.setVisibility(View.VISIBLE);
            ProgressBar progressBar_loading = findViewById(R.id.progressBar_info_detail_loading);
            progressBar_loading.setVisibility(View.GONE);
        }
    }

    @SuppressLint("StaticFieldLeak")
    class InfoDetailAsync extends AsyncTask<Context, Void, Context> {
        String data;
        String[] extraFile;
        int loadSuccess = -1;
        int loadCode = Config.NET_WORK_GET_SUCCESS;

        InfoDetailAsync() {
            this.data = null;
            this.extraFile = null;
        }

        @Override
        protected Context doInBackground(Context... context) {
            try {
                if (context[0] != null) {
                    if (info_source.equals(InfoAdapter.TOPIC_SOURCE_JWC)) {
                        JwcInfoMethod jwcInfoMethod = new JwcInfoMethod(context[0]);
                        loadSuccess = jwcInfoMethod.loadDetail(info_url);
                        if (loadSuccess == Config.NET_WORK_GET_SUCCESS) {
                            data = jwcInfoMethod.getDetail();
                        }
                    } else if (info_source.equals(InfoAdapter.TOPIC_SOURCE_JW)) {
                        JwInfoMethod jwInfoMethod = new JwInfoMethod(context[0]);
                        loadSuccess = jwInfoMethod.loadDetail(info_url);
                        if (loadSuccess == Config.NET_WORK_GET_SUCCESS) {
                            data = jwInfoMethod.getDetail();
                            extraFile = jwInfoMethod.getExtraFileUrl();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                loadCode = Config.NET_WORK_ERROR_CODE_CONNECT_ERROR;
            }
            return context[0];
        }

        @Override
        protected void onPostExecute(Context context) {
            if (BaseMethod.checkNetWorkCode(context, new int[]{loadSuccess}, loadCode)) {
                InfoDetailSet(data, extraFile);
            }
            super.onPostExecute(context);
        }
    }
}