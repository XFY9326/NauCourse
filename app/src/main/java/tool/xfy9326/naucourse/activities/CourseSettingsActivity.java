package tool.xfy9326.naucourse.activities;

import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceFragmentCompat;

import tool.xfy9326.naucourse.fragments.CourseSettingsFragment;

/**
 * Created by xfy9326 on 18-2-20.
 */

public class CourseSettingsActivity extends BaseSettingsActivity {
    private CourseSettingsFragment courseSettingsFragment = null;

    @Override
    protected PreferenceFragmentCompat onCreateFragment() {
        courseSettingsFragment = new CourseSettingsFragment();
        return courseSettingsFragment;
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