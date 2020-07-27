package tool.xfy9326.naucourse.ui.fragments.settings

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import androidx.preference.SeekBarPreference
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.constants.ImageConst
import tool.xfy9326.naucourse.constants.PrefConst
import tool.xfy9326.naucourse.io.prefs.AppPref
import tool.xfy9326.naucourse.io.prefs.SettingsPref
import tool.xfy9326.naucourse.kt.showSnackBar
import tool.xfy9326.naucourse.tools.NotifyBus
import tool.xfy9326.naucourse.tools.NotifyType
import tool.xfy9326.naucourse.ui.dialogs.FullScreenLoadingDialog
import tool.xfy9326.naucourse.ui.fragments.base.BaseSettingsPreferenceFragment
import tool.xfy9326.naucourse.utils.utility.ImageUtils
import tool.xfy9326.naucourse.utils.utility.IntentUtils
import java.util.*

@Suppress("unused")
class CourseTableSettingsFragment : BaseSettingsPreferenceFragment() {
    private val imageMutex = Mutex()

    companion object {
        private const val SELECT_COURSE_TABLE_PICTURE_REQUEST_CODE = 1
    }

    override val preferenceResId = R.xml.settings_course_table
    override val titleName: Int = R.string.settings_course_table

    override fun onPrefViewInit(savedInstanceState: Bundle?) {
        findPreference<CheckBoxPreference>(PrefConst.ShowNextWeekCourseTableAhead)?.setOnPreferenceChangeListener { _, _ ->
            NotifyBus[NotifyType.COURSE_TERM_UPDATE].notifyEvent()
            true
        }

        addRefreshCourseTableListener(PrefConst.ShowNotThisWeekCourseInTable)

        addRefreshCourseTableListener(PrefConst.ForceShowCourseTableWeekends)
        addRefreshCourseTableListener(PrefConst.SameCourseCellHeight)
        addRefreshCourseTableListener(PrefConst.CenterHorizontalShowCourseText)
        addRefreshCourseTableListener(PrefConst.CourseTableRoundCompat)
        addRefreshCourseTableListener(PrefConst.CenterVerticalShowCourseText)
        addRefreshCourseTableListener(PrefConst.UseRoundCornerCourseCell)
        addRefreshCourseTableListener(PrefConst.DrawAllCellBackground)
        addRefreshCourseTableListener(PrefConst.HighLightCourseTableTodayDate)
        addRefreshCourseTableListener(PrefConst.CourseCellTextSize)
        addRefreshCourseTableListener(PrefConst.NotThisWeekCourseShowType)

        addRefreshCourseTableBackgroundListener(PrefConst.CustomCourseTableBackground)
        addRefreshCourseTableBackgroundListener(PrefConst.CourseTableBackgroundScareType)
        addRefreshCourseTableBackgroundListener(PrefConst.CourseTableBackgroundAlpha)
        addRefreshCourseTableBackgroundListener(PrefConst.CourseTableBackgroundFullScreen)

        addRefreshAllTableListener(PrefConst.EnableCourseTableTimeTextColor)
        addRefreshAllTableListener(PrefConst.CourseTableTimeTextColor)
        addRefreshAllTableListener(PrefConst.CustomCourseTableAlpha)

        findPreference<Preference>(PrefConst.ChooseCourseTableBackgroundPicture)?.setOnPreferenceClickListener {
            IntentUtils.selectPicture(this, SELECT_COURSE_TABLE_PICTURE_REQUEST_CODE)
            NotifyBus[NotifyType.REBUILD_COURSE_TABLE_BACKGROUND].notifyEvent()
            false
        }
        findPreference<SeekBarPreference>(PrefConst.CourseTableImageQuality)?.setOnPreferenceChangeListener { _, newValue ->
            modifyCourseTableBackgroundQuality(newValue as Int)
            NotifyBus[NotifyType.REBUILD_COURSE_TABLE_BACKGROUND].notifyEvent()
            true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SELECT_COURSE_TABLE_PICTURE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null && data.data != null) {
                saveCourseTableBackground(data.data!!)
            } else {
                requireActivity().layout_settings.showSnackBar(R.string.picture_choose_cancel)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun modifyCourseTableBackgroundQuality(quality: Int) {
        val imageName = AppPref.CourseTableBackgroundImageName
        if (imageName != null && ImageUtils.localImageExists(imageName, ImageConst.DIR_APP_IMAGE)) {
            lifecycleScope.launch(Dispatchers.IO) {
                imageMutex.withLock {
                    launch(Dispatchers.Main) {
                        FullScreenLoadingDialog.showDialog(childFragmentManager)
                    }

                    val result =
                        ImageUtils.modifyLocalImage(
                            imageName,
                            ImageConst.DIR_APP_IMAGE,
                            quality = quality
                        )

                    launch(Dispatchers.Main) {
                        FullScreenLoadingDialog.close(childFragmentManager)
                        requireActivity().layout_settings.showSnackBar(
                            if (result) {
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
        lifecycleScope.launch(Dispatchers.IO) {
            imageMutex.withLock {
                withContext(Dispatchers.Main) {
                    FullScreenLoadingDialog.showDialog(childFragmentManager)
                }
                AppPref.CourseTableBackgroundImageName?.let {
                    ImageUtils.deleteLocalImage(it, ImageConst.DIR_APP_IMAGE)
                }
                val newImageName = ImageConst.COURSE_TABLE_BACKGROUND_IMAGE_PREFIX + UUID.randomUUID().toString()
                val result = ImageUtils.saveImageToLocalFromUri(
                    newImageName,
                    uri,
                    ImageConst.DIR_APP_IMAGE
                )
                val qualityModifyResult =
                    if (result != null) {
                        ImageUtils.modifyLocalImage(
                            newImageName, ImageConst.DIR_APP_IMAGE, quality =
                            SettingsPref.CourseTableImageQuality
                        )
                    } else {
                        false
                    }
                if (result == null || !qualityModifyResult) {
                    ImageUtils.deleteLocalImage(newImageName, ImageConst.DIR_APP_IMAGE)
                } else {
                    AppPref.CourseTableBackgroundImageName = newImageName
                }
                withContext(Dispatchers.Main) {
                    FullScreenLoadingDialog.close(childFragmentManager)
                    requireActivity().layout_settings.showSnackBar(
                        if (result != null) {
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
        findPreference<Preference>(key)?.setOnPreferenceChangeListener { _, _ ->
            NotifyBus[NotifyType.REBUILD_COURSE_TABLE].notifyEvent()
            true
        }
    }

    private fun addRefreshAllTableListener(key: String) {
        findPreference<Preference>(key)?.setOnPreferenceChangeListener { _, _ ->
            NotifyBus[NotifyType.REBUILD_COURSE_TABLE].notifyEvent()
            NotifyBus[NotifyType.REBUILD_COURSE_TABLE_BACKGROUND].notifyEvent()
            true
        }
    }

    private fun addRefreshCourseTableBackgroundListener(key: String) {
        findPreference<Preference>(key)?.setOnPreferenceChangeListener { _, _ ->
            NotifyBus[NotifyType.REBUILD_COURSE_TABLE_BACKGROUND].notifyEvent()
            true
        }
    }
}