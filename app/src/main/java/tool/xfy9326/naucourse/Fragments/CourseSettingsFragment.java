package tool.xfy9326.naucourse.Fragments;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Handlers.MainHandler;
import tool.xfy9326.naucourse.Methods.ImageMethod;
import tool.xfy9326.naucourse.Methods.PermissionMethod;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.Receivers.UpdateReceiver;

/**
 * Created by xfy9326 on 18-2-20.
 */

public class CourseSettingsFragment extends PreferenceFragmentCompat {
    public static final int WRITE_AND_READ_EXTERNAL_STORAGE_REQUEST_CODE = 1;
    private boolean updateCourseTable = false;
    private boolean reloadTableData = false;
    private boolean cropSuccess = false;
    private float transparency_value;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings_course);
    }

    @Override
    public void onResume() {
        super.onResume();
        PreferenceSet();
    }

    @Override
    public void onDestroy() {
        if (updateCourseTable && getActivity() != null) {
            MainHandler mainHandler = new MainHandler(getActivity());
            if (reloadTableData) {
                mainHandler.sendEmptyMessage(Config.HANDLER_RELOAD_TABLE_DATA);
            } else {
                mainHandler.sendEmptyMessage(Config.HANDLER_RELOAD_TABLE);
            }
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
        Preference.OnPreferenceChangeListener tableDataReloadListener = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                updateCourseTable = true;
                reloadTableData = true;
                return true;
            }
        };

        findPreference(Config.PREFERENCE_SHOW_NEXT_WEEK).setOnPreferenceChangeListener(tableReloadListener);
        findPreference(Config.PREFERENCE_SHOW_WEEKEND).setOnPreferenceChangeListener(tableReloadListener);
        findPreference(Config.PREFERENCE_SHOW_WIDE_TABLE).setOnPreferenceChangeListener(tableReloadListener);
        findPreference(Config.PREFERENCE_COURSE_TABLE_CELL_COLOR).setOnPreferenceChangeListener(tableReloadListener);
        findPreference(Config.PREFERENCE_COURSE_TABLE_SHOW_SINGLE_COLOR).setOnPreferenceChangeListener(tableReloadListener);
        findPreference(Config.PREFERENCE_SHOW_NO_THIS_WEEK_CLASS).setOnPreferenceChangeListener(tableDataReloadListener);

        findPreference(Config.PREFERENCE_COURSE_TABLE_SHOW_BACKGROUND).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if ((boolean) newValue && !cropSuccess) {
                    if (isAdded() && getActivity() != null) {
                        if (PermissionMethod.checkStoragePermission(getActivity(), WRITE_AND_READ_EXTERNAL_STORAGE_REQUEST_CODE)) {
                            chooseAndCropImage();
                        } else {
                            Toast.makeText(getActivity(), R.string.permission_error, Toast.LENGTH_SHORT).show();
                        }
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

        findPreference(Config.PREFERENCE_CHANGE_TABLE_TRANSPARENCY).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                changeTransparency();
                return true;
            }
        });

        findPreference(Config.PREFERENCE_NOTIFY_NEXT_CLASS).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if ((boolean) newValue && getActivity() != null) {
                    Toast.makeText(getActivity(), R.string.ask_lock_background, Toast.LENGTH_SHORT).show();
                    //初始化自动更新
                    getActivity().sendBroadcast(new Intent(UpdateReceiver.UPDATE_ACTION).putExtra(Config.INTENT_IS_ONLY_INIT, true));
                }
                return true;
            }
        });
    }

    public void chooseAndCropImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE);
    }

    private void changeTransparency() {
        if (getActivity() != null) {
            transparency_value = PreferenceManager.getDefaultSharedPreferences(getActivity()).getFloat(Config.PREFERENCE_CHANGE_TABLE_TRANSPARENCY, Config.DEFAULT_PREFERENCE_CHANGE_TABLE_TRANSPARENCY);

            LayoutInflater layoutInflater = getActivity().getLayoutInflater();
            View view = layoutInflater.inflate(R.layout.dialog_edit_progress, (ViewGroup) getActivity().findViewById(R.id.layout_dialog_edit_progress));

            final TextInputEditText textInputEditText = view.findViewById(R.id.editText_dialog_edit_progress);
            textInputEditText.setHint(R.string.transparency);
            textInputEditText.setText(String.valueOf((transparency_value * 100) / 100.0f));
            textInputEditText.setEnabled(false);

            SeekBar seekBar = view.findViewById(R.id.seekBar_dialog_edit_progress);
            seekBar.setMax(100);
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    transparency_value = (float) progress / 100.0f;
                    textInputEditText.setText(String.valueOf(transparency_value));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            seekBar.setProgress((int) (transparency_value * 100));

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.change_table_transparency);
            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putFloat(Config.PREFERENCE_CHANGE_TABLE_TRANSPARENCY, transparency_value).apply();
                    updateCourseTable = true;
                }
            });
            builder.setNegativeButton(android.R.string.cancel, null);
            builder.setNeutralButton(R.string.reset, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().remove(Config.PREFERENCE_CHANGE_TABLE_TRANSPARENCY).apply();
                    updateCourseTable = true;
                }
            });
            builder.setView(view);
            builder.show();
        }
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
