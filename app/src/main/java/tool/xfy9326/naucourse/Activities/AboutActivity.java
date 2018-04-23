package tool.xfy9326.naucourse.Activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import tool.xfy9326.naucourse.BuildConfig;
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

        String version = BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")";
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
