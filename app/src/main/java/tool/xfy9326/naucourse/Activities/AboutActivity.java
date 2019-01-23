package tool.xfy9326.naucourse.Activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import lib.xfy9326.updater.Updater;
import tool.xfy9326.naucourse.BuildConfig;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Methods.NetMethod;
import tool.xfy9326.naucourse.R;

/**
 * Created by xfy9326 on 18-2-20.
 */

public class AboutActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
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

    private void ViewSet() {
        TextView textView_version = findViewById(R.id.textView_about_version);
        TextView textView_open_source = findViewById(R.id.textView_about_open_source);
        TextView textView_feedback = findViewById(R.id.textView_about_feedback);
        TextView textView_donate = findViewById(R.id.textView_about_donate);
        TextView textView_donate_list = findViewById(R.id.textView_about_donate_list);

        String version = "v" + BuildConfig.VERSION_NAME + "-" + Config.SUB_VERSION + " (" + BuildConfig.VERSION_CODE + ") " + Config.VERSION_TYPE;
        version = version.replace(Updater.UPDATE_TYPE_BETA, getString(R.string.beta)).replace(Updater.UPDATE_TYPE_RELEASE, getString(R.string.release)).replace(Config.DEBUG, getString(R.string.debug));

        textView_version.setText(version);

        textView_open_source.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOpenSourceList();
            }
        });
        textView_feedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AboutActivity.this);
                builder.setTitle(R.string.feedback);
                builder.setMessage(getString(R.string.feedback_by_mail, getString(R.string.mail)));
                builder.setPositiveButton(android.R.string.yes, null);
                builder.show();
            }
        });
        textView_donate_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NetMethod.viewUrlInBrowser(AboutActivity.this, "https://www.xfy9326.top/api/donate/naucourse/personList.php");
            }
        });
        textView_donate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AboutActivity.this);
                builder.setTitle(R.string.donate);
                builder.setMessage(R.string.donate_content);
                builder.setPositiveButton(R.string.alipay, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NetMethod.viewUrlInBrowser(AboutActivity.this, Config.DONATE_URL_ALIPAY);
                    }
                });
                builder.setNegativeButton(R.string.wechat, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NetMethod.viewUrlInBrowser(AboutActivity.this, Config.DONATE_URL_WECHAT);
                    }
                });
                builder.setNeutralButton(R.string.qq_wallet, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NetMethod.viewUrlInBrowser(AboutActivity.this, Config.DONATE_URL_QQ_WALLET);
                    }
                });
                builder.show();
            }
        });
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
            View view = layoutInflater.inflate(R.layout.item_open_source_card, (ViewGroup) findViewById(R.id.layout_open_source_card_item));

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
