package tool.xfy9326.naucourse.Activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;
import tool.xfy9326.naucourse.Fragments.CourseSettingsFragment;
import tool.xfy9326.naucourse.R;

/**
 * Created by xfy9326 on 18-2-20.
 */

public class CourseSettingsActivity extends AppCompatActivity {
    private CourseSettingsFragment courseSettingsFragment = null;

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
            courseSettingsFragment = new CourseSettingsFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.layout_settings_content, courseSettingsFragment);
            fragmentTransaction.commit();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CourseSettingsFragment.WRITE_AND_READ_EXTERNAL_STORAGE_REQUEST_CODE) {
            boolean requestSuccess = true;
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_DENIED) {
                    requestSuccess = false;
                    break;
                }
            }
            if (requestSuccess) {
                if (courseSettingsFragment != null) {
                    courseSettingsFragment.chooseAndCropImage();
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (courseSettingsFragment != null) {
            courseSettingsFragment.onActivityResult(requestCode, resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}