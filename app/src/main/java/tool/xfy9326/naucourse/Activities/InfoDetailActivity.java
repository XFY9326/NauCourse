package tool.xfy9326.naucourse.Activities;

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
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tool.xfy9326.naucourse.AsyncTasks.InfoDetailAsync;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Methods.BaseMethod;
import tool.xfy9326.naucourse.Methods.JwInfoMethod;
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
        GetData();
        setContentView(R.layout.activity_info_detail);
        BaseMethod.getBaseApplication(this).setInfoDetailActivity(this);
        ToolBarSet();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (info_source.equals(InfoAdapter.TOPIC_SOURCE_JW)) {
            getMenuInflater().inflate(R.menu.menu_info_detail, menu);
            return true;
        } else {
            return super.onCreateOptionsMenu(menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_info_detail_open_in_browser) {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            Uri content_url = Uri.parse(JwInfoMethod.server_url + info_url);
            intent.setData(content_url);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        BaseMethod.getBaseApplication(this).setInfoDetailActivity(null);
        System.gc();
        super.onDestroy();
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

    public void InfoDetailSet(String content, String[] extraFile) {
        if (content != null) {
            TextView textView_content = findViewById(R.id.textView_info_detail_content);
            if (info_source.equals(InfoAdapter.TOPIC_SOURCE_JWC)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    textView_content.setText(Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY));
                } else {
                    textView_content.setText(Html.fromHtml(content));
                }
            } else if (info_source.equals(InfoAdapter.TOPIC_SOURCE_JW)) {
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