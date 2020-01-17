package tool.xfy9326.naucourse.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.preference.PreferenceManager;

import com.google.android.material.snackbar.Snackbar;

import java.util.Objects;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.asyncTasks.InfoDetailAsync;
import tool.xfy9326.naucourse.beans.info.TopicInfo;
import tool.xfy9326.naucourse.methods.BaseMethod;
import tool.xfy9326.naucourse.methods.ImageMethod;
import tool.xfy9326.naucourse.methods.async.AlstuMethod;
import tool.xfy9326.naucourse.methods.compute.InfoMethod;
import tool.xfy9326.naucourse.methods.io.ShareMethod;
import tool.xfy9326.naucourse.methods.net.NetMethod;
import tool.xfy9326.naucourse.methods.view.DialogMethod;
import tool.xfy9326.naucourse.nausso.NauSSOClient;

/**
 * Created by 10696 on 2018/2/25.
 */

public class InfoDetailActivity extends AppCompatActivity {
    private static final int SHARE_TYPE_URL = 0;
    private static final int SHARE_TYPE_TEXT = 1;
    private static final int SHARE_TYPE_IMAGE = 2;
    @Nullable
    private String infoTitle;
    @Nullable
    private String infoPost;
    @Nullable
    private String infoClick;
    @Nullable
    private String infoDate;
    @Nullable
    private String infoUrl;
    @Nullable
    private String infoSource;
    @Nullable
    private String infoType;
    @Nullable
    private String infoContent = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_detail);
        BaseMethod.getApp(this).setInfoDetailActivity(this);
        if (getIntentData()) {
            toolBarSet();
            viewSet();
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

    private void toolBarSet() {
        setSupportActionBar(findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(infoType);
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
            if (infoUrl != null) {
                if (infoUrl.startsWith("http")) {
                    url = infoUrl;
                } else if (Objects.equals(infoSource, InfoMethod.TOPIC_SOURCE_RSS)) {
                    url = infoUrl;
                } else if (Objects.equals(infoSource, InfoMethod.TOPIC_SOURCE_JWC)) {
                    url = NauSSOClient.JWC_SERVER_URL + infoUrl;
                } else if (Objects.equals(infoSource, InfoMethod.TOPIC_SOURCE_ALSTU)) {
                    url = AlstuMethod.ALSTU_SERVER_URL + infoUrl;
                }
            }

            if (url != null) {
                final String fUrl = url;
                if (item.getItemId() == R.id.menu_info_detail_open_in_browser) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    Uri contentUrl = Uri.parse(url);
                    intent.setData(contentUrl);
                    BaseMethod.runIntent(InfoDetailActivity.this, intent);
                } else if (item.getItemId() == R.id.menu_info_detail_share) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.share_info_detail);
                    builder.setItems(R.array.info_share_type, (dialog, which) -> shareInfo(fUrl, which));
                    builder.show();
                }

            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void shareInfo(String url, int shareType) {
        if (shareType == SHARE_TYPE_URL || shareType == SHARE_TYPE_TEXT) {
            String infoTag = getString(R.string.unknown_post);
            if (infoSource != null) {
                switch (infoSource) {
                    case InfoMethod.TOPIC_SOURCE_JWC:
                        infoTag = getString(R.string.jw_system);
                        break;
                    case InfoMethod.TOPIC_SOURCE_ALSTU:
                        infoTag = getString(R.string.alstu_system);
                        break;
                    case InfoMethod.TOPIC_SOURCE_RSS:
                        infoTag = infoType;
                        break;
                    default:
                }
            }
            String shareText;
            if (shareType == SHARE_TYPE_URL) {
                shareText = String.format("[%s] %s\n%s", infoTag, infoTitle, url);
            } else {
                if (infoContent != null) {
                    shareText = String.format("[%s] %s\n\n%s", infoTag, infoTitle, infoContent);
                } else {
                    Toast.makeText(this, R.string.data_is_loading, Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            ShareMethod.shareText(InfoDetailActivity.this, infoType, shareText);
        } else if (shareType == SHARE_TYPE_IMAGE) {
            Bitmap bitmap = ImageMethod.getViewsBitmap(InfoDetailActivity.this, new View[]{findViewById(R.id.cardView_info_detail_title), findViewById(R.id.layout_info_detail_data)}, true, ResourcesCompat.getColor(getResources(), R.color.info_detail_share_background, getTheme()));
            DialogMethod.showImageShareDialog(InfoDetailActivity.this,
                    bitmap,
                    infoTitle + "_" + infoDate + ".jpeg",
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
                infoUrl = infoDetail.getUrl();
                infoTitle = infoDetail.getTitle();
                infoPost = infoDetail.getPost();
                infoClick = infoDetail.getClick();
                infoDate = infoDetail.getDate();
                infoSource = infoDetail.getSource();
                infoType = infoDetail.getType();
                return true;
            }
        }
        return false;
    }

    private void viewSet() {
        if (NetMethod.isNetworkConnected(this)) {
            getData();
        } else {
            Snackbar.make(findViewById(R.id.layout_info_detail_content), R.string.network_error, Snackbar.LENGTH_SHORT).show();
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences.getBoolean(Config.PREFERENCE_DOWNLOAD_VPN_FILE_WARNING, Config.DEFAULT_PREFERENCE_DOWNLOAD_VPN_FILE_WARNING) && BaseMethod.getApp(this).getClient().isVPNEnabled()) {
            Snackbar.make(findViewById(R.id.layout_info_detail_content), R.string.download_vpn_file, Snackbar.LENGTH_LONG)
                    .setAction(R.string.no_alert_again, v -> sharedPreferences.edit().putBoolean(Config.PREFERENCE_DOWNLOAD_VPN_FILE_WARNING, false).apply())
                    .setActionTextColor(ResourcesCompat.getColor(getResources(), android.R.color.holo_red_light, getTheme()))
                    .show();
        }

        TextView textViewTitle = findViewById(R.id.textView_info_detail_title);
        TextView textViewPost = findViewById(R.id.textView_info_detail_post);
        TextView textViewClick = findViewById(R.id.textView_info_detail_click);
        TextView textViewDate = findViewById(R.id.textView_info_detail_date);

        textViewTitle.setText(infoTitle);
        textViewPost.setText(getString(R.string.info_post, infoPost));
        if (infoClick != null) {
            textViewClick.setText(getString(R.string.info_click, infoClick));
        } else {
            textViewClick.setVisibility(View.GONE);
        }
        textViewDate.setText(getString(R.string.info_date, infoDate));
    }

    public void infoDetailSet(@Nullable String content) {
        if (content != null) {
            TextView textViewContent = findViewById(R.id.textView_info_detail_content);
            if (!content.isEmpty()) {
                try {
                    Spanned spanned;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        spanned = Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY);
                    } else {
                        spanned = Html.fromHtml(content);
                    }
                    infoContent = spanned.toString();
                    textViewContent.setText(spanned);
                } catch (Exception e) {
                    e.printStackTrace();
                    textViewContent.setText(content);
                }
            } else {
                textViewContent.setText(content);
            }
            textViewContent.setMovementMethod(LinkMovementMethod.getInstance());
            textViewContent.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, R.string.data_get_error, Toast.LENGTH_SHORT).show();
        }
        findViewById(R.id.progressBar_info_detail_loading).setVisibility(View.GONE);
    }

    synchronized private void getData() {
        InfoDetailAsync infoDetailAsync = new InfoDetailAsync();
        infoDetailAsync.setData(infoSource, infoUrl);
        infoDetailAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, getApplicationContext());
    }
}