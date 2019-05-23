package tool.xfy9326.naucourse.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.util.List;
import java.util.Objects;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.handlers.MainHandler;
import tool.xfy9326.naucourse.methods.BaseMethod;
import tool.xfy9326.naucourse.methods.ImageMethod;
import tool.xfy9326.naucourse.methods.PermissionMethod;
import tool.xfy9326.naucourse.receivers.CourseUpdateReceiver;
import tool.xfy9326.naucourse.tools.IO;
import tool.xfy9326.naucourse.views.ViewPagerAdapter;

/**
 * Created by xfy9326 on 18-2-20.
 */

public class CourseSettingsFragment extends PreferenceFragmentCompat {
    public static final int WRITE_AND_READ_EXTERNAL_STORAGE_REQUEST_CODE = 1;
    private static final int PICK_IMAGE_CHOOSER_REQUEST_CODE = 2;
    private static final int CROP_IMAGE_ACTIVITY_REQUEST_CODE = 3;
    private boolean updateCourseTable = false;
    private boolean reloadTableData = false;
    private boolean cropSuccess = false;
    private float transparency_value;
    private String imageTempPath;

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
        Preference.OnPreferenceChangeListener tableReloadListener = (preference, newValue) -> {
            updateCourseTable = true;
            return true;
        };
        Preference.OnPreferenceChangeListener tableDataReloadListener = (preference, newValue) -> {
            updateCourseTable = true;
            reloadTableData = true;
            return true;
        };

        ((Preference) Objects.requireNonNull(findPreference(Config.PREFERENCE_SHOW_NEXT_WEEK))).setOnPreferenceChangeListener(tableReloadListener);
        ((Preference) Objects.requireNonNull(findPreference(Config.PREFERENCE_SHOW_WIDE_TABLE))).setOnPreferenceChangeListener(tableReloadListener);
        ((Preference) Objects.requireNonNull(findPreference(Config.PREFERENCE_COURSE_TABLE_CELL_COLOR))).setOnPreferenceChangeListener(tableReloadListener);
        ((Preference) Objects.requireNonNull(findPreference(Config.PREFERENCE_COURSE_TABLE_SHOW_SINGLE_COLOR))).setOnPreferenceChangeListener(tableReloadListener);
        ((Preference) Objects.requireNonNull(findPreference(Config.PREFERENCE_SHOW_NO_THIS_WEEK_CLASS))).setOnPreferenceChangeListener(tableDataReloadListener);

        ((Preference) Objects.requireNonNull(findPreference(Config.PREFERENCE_COURSE_TABLE_SHOW_BACKGROUND))).setOnPreferenceChangeListener((preference, newValue) -> {
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
        });

        ((Preference) Objects.requireNonNull(findPreference(Config.PREFERENCE_CHANGE_TABLE_TRANSPARENCY))).setOnPreferenceClickListener(preference -> {
            changeTransparency();
            return true;
        });

        ((Preference) Objects.requireNonNull(findPreference(Config.PREFERENCE_NOTIFY_NEXT_CLASS))).setOnPreferenceChangeListener((preference, newValue) -> {
            if ((boolean) newValue && getActivity() != null) {
                Toast.makeText(getActivity(), R.string.ask_lock_background, Toast.LENGTH_SHORT).show();
                //初始化自动更新
                getActivity().sendBroadcast(new Intent(CourseUpdateReceiver.UPDATE_ACTION).putExtra(Config.INTENT_IS_ONLY_INIT, true));
            }
            return true;
        });
    }

    public void chooseAndCropImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        try {
            startActivityForResult(intent, PICK_IMAGE_CHOOSER_REQUEST_CODE);
        } catch (Exception e) {
            e.printStackTrace();
            if (getContext() != null) {
                Toast.makeText(getContext(), R.string.image_chooser_no_found, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void changeTransparency() {
        if (getActivity() != null) {
            final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            transparency_value = sharedPreferences.getFloat(Config.PREFERENCE_CHANGE_TABLE_TRANSPARENCY, Config.DEFAULT_PREFERENCE_CHANGE_TABLE_TRANSPARENCY);

            LayoutInflater layoutInflater = getActivity().getLayoutInflater();
            View view = layoutInflater.inflate(R.layout.dialog_edit_progress, getActivity().findViewById(R.id.layout_dialog_edit_progress));

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
            builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
                sharedPreferences.edit().putFloat(Config.PREFERENCE_CHANGE_TABLE_TRANSPARENCY, transparency_value).apply();
                updateCourseTable = true;
            });
            builder.setNegativeButton(android.R.string.cancel, null);
            builder.setNeutralButton(R.string.reset, (dialog, which) -> {
                sharedPreferences.edit().remove(Config.PREFERENCE_CHANGE_TABLE_TRANSPARENCY).apply();
                updateCourseTable = true;
            });
            builder.setView(view);
            builder.show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_CANCELED) {
            if (requestCode == PICK_IMAGE_CHOOSER_REQUEST_CODE) {
                if (isAdded() && getActivity() != null && data != null) {
                    Uri uri = data.getData();
                    if (uri != null) {
                        ViewPagerAdapter viewPagerAdapter = BaseMethod.getApp(getActivity()).getViewPagerAdapter();
                        if (viewPagerAdapter != null) {
                            TableFragment tableFragment = viewPagerAdapter.getTableFragment();
                            if (tableFragment != null) {
                                int height = tableFragment.getTableHeight();
                                int width = tableFragment.getTableWidth();
                                if (height > 0 && width > 0) {
                                    Intent intent = new Intent("com.android.camera.action.CROP");
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    } else {
                                        List<ResolveInfo> resInfoList = getActivity().getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                                        for (ResolveInfo resolveInfo : resInfoList) {
                                            String packageName = resolveInfo.activityInfo.packageName;
                                            getActivity().grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                        }
                                    }
                                    intent.setDataAndType(uri, "image/*");
                                    intent.putExtra("crop", "true");
                                    intent.putExtra("aspectX", width);
                                    intent.putExtra("aspectY", height);
                                    intent.putExtra("outputX", width);
                                    intent.putExtra("outputY", height);
                                    intent.putExtra("scale", true);
                                    intent.putExtra("return-data", false);
                                    intent.putExtra("noFaceDetection", false);
                                    intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
                                    imageTempPath = ImageMethod.getCourseTableBackgroundImageTempPath(getActivity());
                                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.parse("file:///" + imageTempPath));

                                    try {
                                        startActivityForResult(intent, CROP_IMAGE_ACTIVITY_REQUEST_CODE);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        if (getContext() != null) {
                                            Toast.makeText(getContext(), R.string.image_crop_no_found, Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    super.onActivityResult(requestCode, resultCode, data);
                                    return;
                                }
                            }
                        }
                    }
                    Toast.makeText(getActivity(), R.string.image_get_error, Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == CROP_IMAGE_ACTIVITY_REQUEST_CODE && data != null) {
                if (resultCode == Activity.RESULT_OK && getActivity() != null && imageTempPath != null) {
                    if (IO.copyFile(imageTempPath, ImageMethod.getCourseTableBackgroundImagePath(getActivity()), true)) {
                        cropSuccess = true;
                        ((CheckBoxPreference) Objects.requireNonNull(findPreference(Config.PREFERENCE_COURSE_TABLE_SHOW_BACKGROUND))).setChecked(true);
                        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putBoolean(Config.PREFERENCE_COURSE_TABLE_SHOW_BACKGROUND, true).apply();
                        updateCourseTable = true;
                        imageTempPath = null;
                    } else {
                        Toast.makeText(getActivity(), R.string.image_get_error, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
