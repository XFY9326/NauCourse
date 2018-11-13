package tool.xfy9326.naucourse.Activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;
import tool.xfy9326.naucourse.Fragments.GlobalSettingsFragment;
import tool.xfy9326.naucourse.R;

public class GlobalSettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ToolBarSet();
        setFragment(savedInstanceState);
    }

    private void ToolBarSet() {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setFragment(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            GlobalSettingsFragment globalSettingsFragment = new GlobalSettingsFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.layout_settings_content, globalSettingsFragment);
            fragmentTransaction.commit();
        }
    }
}
