package tool.xfy9326.naucourse.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import java.util.Objects;

import lib.xfy9326.nausso.NauSSOClient;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.asyncTasks.InfoDetailAsync;
import tool.xfy9326.naucourse.methods.BaseMethod;
import tool.xfy9326.naucourse.methods.DialogMethod;
import tool.xfy9326.naucourse.methods.ImageMethod;
import tool.xfy9326.naucourse.methods.InfoMethod;
import tool.xfy9326.naucourse.methods.NetMethod;
import tool.xfy9326.naucourse.methods.netInfoMethods.AlstuMethod;
import tool.xfy9326.naucourse.utils.TopicInfo;

/**
 * Created by 10696 on 2018/2/25.
 */

public class InfoDetailActivity extends AppCompatActivity {
    private static final int SHARE_TYPE_URL = 0;
    private static final int SHARE_TYPE_TEXT = 1;
    private static final int SHARE_TYPE_IMAGE = 2;
    @Nullable
    private String info_title;
    @Nullable
    private String info_post;
    @Nullable
    private String info_click;
    @Nullable
    private String info_date;
    @Nullable
    private String info_url;
    @Nullable
    private String info_source;
    @Nullable
    private String info_type;
    @Nullable
    private String info_content = null;

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

    @Override
    protected void onDestroy() {
        BaseMethod.getApp(this).setInfoDetailActivity(null);
        System.gc();
        super.onDestroy();
    }

    private void ToolBarSet() {
        setSupportActionBar(findViewById(R.id.toolbar));
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_info_detail_open_in_browser || item.getItemId() == R.id.menu_info_detail_share) {
            String url = null;
            if (Objects.equals(info_source, InfoMethod.TOPIC_SOURCE_RSS)) {
                url = info_url;
            } else if (Objects.equals(info_source, InfoMethod.TOPIC_SOURCE_JWC)) {
                url = NauSSOClient.JWC_SERVER_URL + info_url;
            } else if (Objects.equals(info_source, InfoMethod.TOPIC_SOURCE_ALSTU)) {
                url = AlstuMethod.ALSTU_SERVER_URL + info_url;
            }

            if (url != null) {
                final String f_url = url;
                if (item.getItemId() == R.id.menu_info_detail_open_in_browser) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    Uri content_url = Uri.parse(url);
                    intent.setData(content_url);
                    BaseMethod.runIntent(InfoDetailActivity.this, intent);
                } else if (item.getItemId() == R.id.menu_info_detail_share) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.share_info_detail);
                    builder.setItems(R.array.info_share_type, (dialog, which) -> shareInfo(f_url, which));
                    builder.show();
                }

            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void shareInfo(String url, int shareType) {
        final Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        if (shareType == SHARE_TYPE_URL || shareType == SHARE_TYPE_TEXT) {
            String infoTag = getString(R.string.unknown_post);
            if (info_source != null) {
                switch (info_source) {
                    case InfoMethod.TOPIC_SOURCE_JWC:
                        infoTag = getString(R.string.jw_system);
                        break;
                    case InfoMethod.TOPIC_SOURCE_ALSTU:
                        infoTag = getString(R.string.alstu_system);
                        break;
                    case InfoMethod.TOPIC_SOURCE_RSS:
                        infoTag = info_type;
                        break;
                }
            }
            String shareText;
            if (shareType == SHARE_TYPE_URL) {
                shareText = String.format("[%s] %s\n%s", infoTag, info_title, url);
            } else {
                if (info_content != null) {
                    shareText = String.format("[%s] %s\n\n%s", infoTag, info_title, info_content);
                } else {
                    Toast.makeText(this, R.string.data_is_loading, Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, info_type);
            intent.putExtra(Intent.EXTRA_TEXT, shareText);
            BaseMethod.runIntent(InfoDetailActivity.this, Intent.createChooser(intent, getString(R.string.share)));
        } else if (shareType == SHARE_TYPE_IMAGE) {
            Bitmap bitmap = ImageMethod.getViewsBitmap(InfoDetailActivity.this, new View[]{findViewById(R.id.cardView_info_detail_title), findViewById(R.id.layout_info_detail_data)}, true);
            DialogMethod.showImageShareDialog(InfoDetailActivity.this,
                    bitmap,
                    Config.PICTURE_DICTIONARY_PATH + Config.INFO_STU_PHOTO_FILE_NAME,
                    R.string.share_info_detail,
                    R.string.share_info_failed,
                    R.string.share_info);
        }
    }

    private boolean getIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            TopicInfo infoDetail = (TopicInfo) intent.getSerializableExtra(Config.INTENT_INFO_DETAIL);
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

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences.getBoolean(Config.PREFERENCE_DOWNLOAD_VPN_FILE_WARNING, Config.DEFAULT_PREFERENCE_DOWNLOAD_VPN_FILE_WARNING) && BaseMethod.getApp(this).getClient().isVPNEnabled()) {
            Snackbar.make(findViewById(R.id.layout_info_detail_content), R.string.download_vpn_file, Snackbar.LENGTH_LONG)
                    .setAction(R.string.no_alert_again, v -> sharedPreferences.edit().putBoolean(Config.PREFERENCE_DOWNLOAD_VPN_FILE_WARNING, false).apply())
                    .setActionTextColor(getResources().getColor(android.R.color.holo_red_light))
                    .show();
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

    public void InfoDetailSet(@Nullable String content) {
        if (content != null) {
            TextView textView_content = findViewById(R.id.textView_info_detail_content);
            Spanned spanned;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                spanned = Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY);
            } else {
                spanned = Html.fromHtml(content);
            }
            info_content = spanned.toString();
            textView_content.setText(spanned);
            textView_content.setMovementMethod(LinkMovementMethod.getInstance());
            textView_content.setVisibility(View.VISIBLE);
            ProgressBar progressBar_loading = findViewById(R.id.progressBar_info_detail_loading);
            progressBar_loading.setVisibility(View.GONE);
        }
    }

    synchronized private void getData() {
        InfoDetailAsync infoDetailAsync = new InfoDetailAsync();
        infoDetailAsync.setData(info_source, info_url);
        infoDetailAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, getApplicationContext());
    }

}