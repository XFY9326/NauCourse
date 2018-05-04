package tool.xfy9326.naucourse.Fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Handlers.MainHandler;
import tool.xfy9326.naucourse.Methods.ImageMethod;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.Receivers.UpdateReceiver;

/**
 * Created by xfy9326 on 18-2-20.
 */

public class SettingsFragment extends PreferenceFragment {
    private boolean updateCourseTable = false;
    private final int WRITE_AND_READ_EXTERNAL_STORAGE_REQUEST_CODE = 1;
    private boolean cropSuccess = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_global);
        PreferenceSet();
    }

    @Override
    public void onDestroy() {
        if (updateCourseTable) {
            MainHandler mainHandler = new MainHandler(getActivity());
            mainHandler.sendEmptyMessage(Config.HANDLER_RELOAD_TABLE);
        }
        super.onDestroy();
    }

    private void PreferenceSet() {
        Preference.OnPreferenceChangeListener tableReloadListener = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                updateCourseTable = true;
                return true;
            }
        };

        findPreference(Config.PREFERENCE_SHOW_NEXT_WEEK).setOnPreferenceChangeListener(tableReloadListener);
        findPreference(Config.PREFERENCE_SHOW_WEEKEND).setOnPreferenceChangeListener(tableReloadListener);
        findPreference(Config.PREFERENCE_SHOW_WIDE_TABLE).setOnPreferenceChangeListener(tableReloadListener);
        findPreference(Config.PREFERENCE_COURSE_TABLE_CELL_COLOR).setOnPreferenceChangeListener(tableReloadListener);

        findPreference(Config.PREFERENCE_COURSE_TABLE_SHOW_BACKGROUND).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if ((boolean) newValue && !cropSuccess) {
                    if (isAdded() && getActivity() != null && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                            || ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, WRITE_AND_READ_EXTERNAL_STORAGE_REQUEST_CODE);
                        Toast.makeText(getActivity(), R.string.permission_error, Toast.LENGTH_SHORT).show();
                    } else {
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("image/*");
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        startActivityForResult(intent, CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE);
                    }
                    updateCourseTable = true;
                    return false;
                } else {
                    if (getActivity() != null) {
                        File file = new File(ImageMethod.getCourseTableBackgroundImagePath(getActivity()));
                        if (file.exists()) {
                            //noinspection ResultOfMethodCallIgnored
                            file.delete();
                        }
                    }
                    cropSuccess = false;
                    updateCourseTable = true;
                    return true;
                }
            }
        });

        findPreference(Config.PREFERENCE_NOTIFY_NEXT_CLASS).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if ((boolean) newValue) {
                    Toast.makeText(getActivity(), R.string.ask_lock_background, Toast.LENGTH_SHORT).show();
                    //初始化自动更新
                    getActivity().sendBroadcast(new Intent(UpdateReceiver.UPDATE_ACTION).putExtra(Config.INTENT_IS_ONLY_INIT, true));
                }
                return true;
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_CANCELED) {
            if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE) {
                if (isAdded() && getActivity() != null && data != null) {
                    Uri imageUri = CropImage.getPickImageResultUri(getActivity(), data);
                    Uri outputUri = Uri.fromFile(new File(ImageMethod.getCourseTableBackgroundImagePath(getActivity())));
                    CropImage.activity(imageUri)
                            .setAllowRotation(false)
                            .setOutputCompressQuality(80)
                            .setOutputUri(outputUri)
                            .setMultiTouchEnabled(true)
                            .setAutoZoomEnabled(true)
                            .setActivityTitle(getString(R.string.course_table_background))
                            .start(getActivity());
                }
            } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && data != null) {
                if (resultCode == Activity.RESULT_OK && getActivity() != null) {
                    cropSuccess = true;
                    ((CheckBoxPreference) findPreference(Config.PREFERENCE_COURSE_TABLE_SHOW_BACKGROUND)).setChecked(true);
                    PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putBoolean(Config.PREFERENCE_COURSE_TABLE_SHOW_BACKGROUND, true).apply();
                    updateCourseTable = true;
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
