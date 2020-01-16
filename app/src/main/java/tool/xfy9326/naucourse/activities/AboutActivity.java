package tool.xfy9326.naucourse.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import java.util.Calendar;
import java.util.Locale;

import tool.xfy9326.naucourse.BuildConfig;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.methods.BaseMethod;
import tool.xfy9326.naucourse.methods.net.NetMethod;
import tool.xfy9326.naucourse.methods.view.DialogMethod;
import tool.xfy9326.naucourse.tools.Updater;

/**
 * Created by xfy9326 on 18-2-20.
 *
 * @author xfy9326
 */

public class AboutActivity extends AppCompatActivity {
    private static final int ICON_CLICK_MAX_COUNT = 5;
    private int iconClickCount = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        toolBarSet();
        viewSet();
    }

    private void toolBarSet() {
        setSupportActionBar(findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_about, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_about_open_source_license:
                DialogMethod.showLicenseDialog(this);
                break;
            case R.id.menu_about_open_source:
                showOpenSourceList();
                break;
            case R.id.menu_about_donate:
                DialogMethod.showDonateDialog(this);
                break;
            case R.id.menu_about_new_version_info:
                BaseMethod.showNewVersionInfo(AboutActivity.this, false);
                break;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    private void viewSet() {
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(AboutActivity.this);

        TextView textViewVersion = findViewById(R.id.textView_about_version);
        TextView textViewCopyRight = findViewById(R.id.textView_about_copy_right);

        textViewCopyRight.setText(getString(R.string.copyright, Calendar.getInstance().get(Calendar.YEAR)));

        String version = String.format(Locale.CHINA, "v%s.%d (%d) %s", BuildConfig.VERSION_NAME, Config.SUB_VERSION, BuildConfig.VERSION_CODE, Config.VERSION_TYPE);
        version = version.replace(Updater.UPDATE_TYPE_BETA, getString(R.string.beta)).replace(Updater.UPDATE_TYPE_RELEASE, getString(R.string.release)).replace(Config.DEBUG, getString(R.string.debug));

        textViewVersion.setText(version);

        if (!BuildConfig.DEBUG) {
            findViewById(R.id.imageView_about_app_icon).setOnClickListener(v -> {
                if (!sharedPreferences.getBoolean(Config.PREFERENCE_SHOW_HIDDEN_FUNCTION, Config.DEFAULT_PREFERENCE_SHOW_HIDDEN_FUNCTION)) {
                    iconClickCount++;
                    if (iconClickCount >= ICON_CLICK_MAX_COUNT) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(AboutActivity.this);
                        builder.setTitle(R.string.attention);
                        builder.setCancelable(false);
                        builder.setMessage(R.string.unlock_attention);
                        builder.setPositiveButton(R.string.accept, (dialog, which) -> {
                            Toast.makeText(AboutActivity.this, R.string.unlock_success, Toast.LENGTH_SHORT).show();
                            sharedPreferences.edit().putBoolean(Config.PREFERENCE_SHOW_HIDDEN_FUNCTION, true).apply();
                        });
                        builder.setNegativeButton(R.string.reject, null);
                        builder.show();
                    } else {
                        Toast.makeText(AboutActivity.this, String.format(Locale.CHINA, "%d/%d", iconClickCount, ICON_CLICK_MAX_COUNT), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AboutActivity.this, R.string.unlock_already_success, Toast.LENGTH_SHORT).show();
                }
            });
        }

        findViewById(R.id.textView_about_feedback).setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(AboutActivity.this);
            builder.setTitle(R.string.feedback);
            builder.setMessage(getString(R.string.feedback_methods, getString(R.string.mail), getString(R.string.qq_group)));
            builder.setPositiveButton(android.R.string.yes, null);
            builder.setNeutralButton(R.string.feedback_platform, (dialog, which) -> NetMethod.viewUrlInBrowser(AboutActivity.this, Config.APP_FEEDBACK_URL));
            builder.show();
        });

        findViewById(R.id.textView_about_eula).setOnClickListener(v -> DialogMethod.showEULADialog(AboutActivity.this, false, null));
    }

    //开源资源列表显示
    private void showOpenSourceList() {
        LayoutInflater layoutInflater = getLayoutInflater();

        LinearLayout linearLayout = new LinearLayout(AboutActivity.this);
        linearLayout.setPadding(10, 30, 10, 25);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        String[] names = getResources().getStringArray(R.array.open_source_name);
        String[] webs = getResources().getStringArray(R.array.open_source_web);

        for (int i = 0; i < names.length; i++) {
            View view = layoutInflater.inflate(R.layout.item_open_source_card, findViewById(R.id.layout_open_source_card_item));

            ((TextView) view.findViewById(R.id.textView_open_source_name)).setText(names[i]);
            ((TextView) view.findViewById(R.id.textView_open_source_web)).setText(webs[i]);

            linearLayout.addView(view);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(AboutActivity.this);
        builder.setTitle(R.string.open_source);
        builder.setView(linearLayout);
        builder.show();
    }
}
