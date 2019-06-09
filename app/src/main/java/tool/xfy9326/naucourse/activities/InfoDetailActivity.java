package tool.xfy9326.naucourse.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
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
import androidx.core.content.FileProvider;

import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import lib.xfy9326.nausso.NauSSOClient;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.asyncTasks.InfoDetailAsync;
import tool.xfy9326.naucourse.methods.BaseMethod;
import tool.xfy9326.naucourse.methods.ImageMethod;
import tool.xfy9326.naucourse.methods.InfoMethod;
import tool.xfy9326.naucourse.methods.NetMethod;
import tool.xfy9326.naucourse.methods.PermissionMethod;
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
                    runIntent(intent);
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
                    shareText = String.format("[%s] %s\n%s", infoTag, info_title, info_content);
                } else {
                    Toast.makeText(this, R.string.data_is_loading, Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, info_type);
            intent.putExtra(Intent.EXTRA_TEXT, shareText);
            runIntent(Intent.createChooser(intent, getString(R.string.share)));
        } else if (shareType == SHARE_TYPE_IMAGE) {
            if (PermissionMethod.checkStoragePermission(InfoDetailActivity.this, 0)) {
                Bitmap bitmap = ImageMethod.getViewsBitmap(InfoDetailActivity.this, new View[]{findViewById(R.id.cardView_info_detail_title), findViewById(R.id.layout_info_detail_data)}, true);

                if (bitmap != null) {
                    final String path = Config.PICTURE_TEMP_DICTIONARY_PATH + Config.INFO_DETAIL_IMAGE_FILE_NAME;
                    LayoutInflater layoutInflater = getLayoutInflater();
                    View view = layoutInflater.inflate(R.layout.dialog_share_image, findViewById(R.id.layout_dialog_share_image));
                    PhotoView photoView = view.findViewById(R.id.photoView_share_image);
                    photoView.setImageDrawable(new BitmapDrawable(getResources(), bitmap));

                    AlertDialog.Builder builder = new AlertDialog.Builder(InfoDetailActivity.this);
                    builder.setView(view);
                    builder.setTitle(R.string.share_info_detail);
                    builder.setPositiveButton(R.string.share, (dialog, which) -> {
                        try {
                            if (ImageMethod.saveBitmap(bitmap, path, false)) {
                                Uri photoURI = FileProvider.getUriForFile(InfoDetailActivity.this, Config.FILE_PROVIDER_AUTH, new File(path));
                                intent.setType("image/*");
                                intent.putExtra(Intent.EXTRA_STREAM, photoURI);
                                intent.addCategory(Intent.CATEGORY_DEFAULT);
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                runIntent(Intent.createChooser(intent, getString(R.string.share_course_table)));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(InfoDetailActivity.this, R.string.share_info_failed, Toast.LENGTH_SHORT).show();
                        }
                    });
                    builder.setNeutralButton(R.string.save, (dialog, which) -> {
                        try {
                            if (ImageMethod.saveBitmap(bitmap, path, false)) {
                                Toast.makeText(InfoDetailActivity.this, getString(R.string.save_file_success, path), Toast.LENGTH_SHORT).show();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(InfoDetailActivity.this, R.string.save_failed, Toast.LENGTH_SHORT).show();
                        }
                    });
                    builder.setNegativeButton(android.R.string.cancel, null);
                    builder.setOnCancelListener(dialog -> {
                        if (!bitmap.isRecycled()) {
                            bitmap.recycle();
                        }
                    });
                    builder.show();
                }
            } else {
                Toast.makeText(InfoDetailActivity.this, R.string.share_info_failed, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(InfoDetailActivity.this, R.string.permission_error, Toast.LENGTH_SHORT).show();
        }
    }


    private void runIntent(Intent intent) {
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.no_available_application, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
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