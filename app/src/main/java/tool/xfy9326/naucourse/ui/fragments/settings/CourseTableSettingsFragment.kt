package tool.xfy9326.naucourse.ui.fragments.settings

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.preference.Preference
import androidx.preference.SeekBarPreference
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tool.xfy9326.naucourse.App
import tool.xfy9326.naucourse.Constants
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.io.prefs.SettingsPref
import tool.xfy9326.naucourse.ui.dialogs.FullScreenLoadingDialog
import tool.xfy9326.naucourse.ui.fragments.base.BaseSettingsPreferenceFragment
import tool.xfy9326.naucourse.utils.utility.ImageUtils
import tool.xfy9326.naucourse.utils.utility.IntentUtils
import tool.xfy9326.naucourse.utils.views.ActivityUtils.showSnackBar

@Suppress("unused")
class CourseTableSettingsFragment : BaseSettingsPreferenceFragment(), Preference.OnPreferenceChangeListener {
    private val ioScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val imageMutex = Mutex()

    companion object {
        private const val SELECT_COURSE_TABLE_PICTURE_REQUEST_CODE = 1
    }

    override val preferenceResId = R.xml.settings_course_table

    override fun onPrefViewInit(savedInstanceState: Bundle?) {
        addRefreshCourseTableListener(Constants.Pref.ForceShowCourseTableWeekends)
        addRefreshCourseTableListener(Constants.Pref.SameCourseCellHeight)
        addRefreshCourseTableListener(Constants.Pref.CenterHorizontalShowCourseText)
        addRefreshCourseTableListener(Constants.Pref.CourseTableRoundCompat)
        addRefreshCourseTableListener(Constants.Pref.CenterVerticalShowCourseText)
        addRefreshCourseTableListener(Constants.Pref.UseRoundCornerCourseCell)
        addRefreshCourseTableListener(Constants.Pref.DrawAllCellBackground)
        addRefreshCourseTableListener(Constants.Pref.CustomCourseTableBackground)
        addRefreshCourseTableListener(Constants.Pref.CustomCourseTableAlpha)
        addRefreshCourseTableListener(Constants.Pref.CourseTableBackgroundScareType)
        addRefreshCourseTableListener(Constants.Pref.CourseTableBackgroundAlpha)

        findPreference<Preference>(Constants.Pref.ChooseCourseTableBackgroundPicture)?.setOnPreferenceClickListener {
            IntentUtils.selectPicture(this, SELECT_COURSE_TABLE_PICTURE_REQUEST_CODE)
            App.instance.requestRebuildCourseTable.notifyEvent()
            false
        }
        findPreference<SeekBarPreference>(Constants.Pref.CourseTableImageQuality)?.setOnPreferenceChangeListener { _, newValue ->
            modifyCourseTableBackgroundQuality(newValue as Int)
            App.instance.requestRebuildCourseTable.notifyEvent()
            true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SELECT_COURSE_TABLE_PICTURE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null && data.data != null) {
                saveCourseTableBackground(data.data!!)
            } else {
                showSnackBar(requireActivity().layout_settings, R.string.picture_choose_cancel)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun modifyCourseTableBackgroundQuality(quality: Int) {
        if (ImageUtils.localImageExists(Constants.Image.COURSE_TABLE_BACKGROUND_IMAGE_NAME, Constants.Image.DIR_APP_IMAGE)) {
            ioScope.launch {
                imageMutex.withLock {
                    launch(Dispatchers.Main) {
                        FullScreenLoadingDialog().show(childFragmentManager, FullScreenLoadingDialog.LOADING_DIALOG_TAG)
                    }

                    val result =
                        ImageUtils.modifyLocalImage(
                            Constants.Image.COURSE_TABLE_BACKGROUND_IMAGE_NAME,
                            Constants.Image.DIR_APP_IMAGE,
                            quality = quality
                        )

                    launch(Dispatchers.Main) {
                        (childFragmentManager.findFragmentByTag(FullScreenLoadingDialog.LOADING_DIALOG_TAG) as DialogFragment?)?.dismissAllowingStateLoss()
                        showSnackBar(
                            requireActivity().layout_settings, if (result) {
                                R.string.picture_quality_modify_success
                            } else {
                                R.string.picture_quality_modify_failed
                            }
                        )
                    }
                }
            }
        }
    }

    private fun saveCourseTableBackground(uri: Uri) {
        ioScope.launch {
            imageMutex.withLock {
                launch(Dispatchers.Main) {
                    FullScreenLoadingDialog().show(childFragmentManager, FullScreenLoadingDialog.LOADING_DIALOG_TAG)
                }
                val result = ImageUtils.saveImageToLocalFromUri(
                    Constants.Image.COURSE_TABLE_BACKGROUND_IMAGE_NAME,
                    uri,
                    Constants.Image.DIR_APP_IMAGE
                )
                val qualityModifyResult =
                    if (result != null) {
                        ImageUtils.modifyLocalImage(
                            Constants.Image.COURSE_TABLE_BACKGROUND_IMAGE_NAME, Constants.Image.DIR_APP_IMAGE, quality =
                            SettingsPref.CourseTableImageQuality
                        )
                    } else {
                        false
                    }
                if (result == null || !qualityModifyResult) {
                    ImageUtils.deleteLocalImage(Constants.Image.COURSE_TABLE_BACKGROUND_IMAGE_NAME, Constants.Image.DIR_APP_IMAGE)
                }
                launch(Dispatchers.Main) {
                    (childFragmentManager.findFragmentByTag(FullScreenLoadingDialog.LOADING_DIALOG_TAG) as DialogFragment?)?.dismissAllowingStateLoss()
                    showSnackBar(
                        requireActivity().layout_settings, if (result != null) {
                            if (qualityModifyResult) {
                                R.string.course_table_background_set_success
                            } else {
                                R.string.picture_quality_modify_failed
                            }
                        } else {
                            R.string.picture_choose_error
                        }
                    )
                }
            }
        }
    }

    private fun addRefreshCourseTableListener(key: String) {
        findPreference<Preference>(key)?.onPreferenceChangeListener = this
    }

    // 刷新课表视图
    override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
        App.instance.requestRebuildCourseTable.notifyEvent()
        return true
    }

    override fun onDestroy() {
        ioScope.cancel()
        super.onDestroy()
    }
}