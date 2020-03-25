package tool.xfy9326.naucourse.compat

import android.annotation.SuppressLint
import android.content.Context
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tool.xfy9326.naucourse.App
import tool.xfy9326.naucourse.Constants
import tool.xfy9326.naucourse.beans.CourseCellStyle
import tool.xfy9326.naucourse.beans.UserInfo
import tool.xfy9326.naucourse.compat.beans.CourseCompat
import tool.xfy9326.naucourse.compat.beans.CourseDetailCompat
import tool.xfy9326.naucourse.network.clients.base.LoginInfo
import tool.xfy9326.naucourse.providers.beans.jwc.*
import tool.xfy9326.naucourse.providers.info.methods.CourseInfo
import tool.xfy9326.naucourse.providers.store.CourseCellStyleStore
import tool.xfy9326.naucourse.utils.compute.CourseUtils
import tool.xfy9326.naucourse.utils.debug.ExceptionUtils
import tool.xfy9326.naucourse.utils.debug.LogUtils
import tool.xfy9326.naucourse.utils.secure.AccountUtils
import tool.xfy9326.naucourse.utils.utility.IOUtils
import java.io.File


object OldDataCompat {
    private val CourseDataPath = App.instance.filesDir.absolutePath + File.separator + "Course.txn"

    suspend fun applyCompatDataToCurrentStore() = withContext(Dispatchers.IO) {
        val loginInfo = readLoginInfoFromOld()
        val courseData =
            if (loginInfo != null) {
                readCourseDataFromOld(loginInfo.userId)
            } else {
                null
            }

        clearOldData()

        if (loginInfo != null) {
            AccountUtils.saveUserInfo(UserInfo(loginInfo.userId, loginInfo.userPw))
        }

        if (courseData != null) {
            CourseInfo.saveNewCourses(courseData.first)
            CourseCellStyleStore.saveStore(courseData.second)
        }

        return@withContext loginInfo
    }

    private fun readCourseDataFromOld(userId: String): Pair<CourseSet, Array<CourseCellStyle>>? {
        try {
            val text = IOUtils.readTextFromFile(CourseDataPath)
            if (!text.isNullOrEmpty()) {
                val decryptText = AESCompat.decrypt(text, userId)
                if (decryptText != null) {
                    val type = object : TypeToken<ArrayList<CourseCompat>>() {}.type
                    val data = Gson().fromJson<ArrayList<CourseCompat>>(decryptText, type)
                    return convertOldDataToCourseSet(App.instance, data)
                }
            }
        } catch (e: Exception) {
            ExceptionUtils.printStackTrace<OldDataCompat>(e)
            LogUtils.w<OldDataCompat>("Course Data Compat Failed!")
        }
        LogUtils.w<OldDataCompat>("Course Data Compat Empty!")
        return null
    }

    private fun convertOldDataToCourseSet(context: Context, oldData: ArrayList<CourseCompat>): Pair<CourseSet, Array<CourseCellStyle>>? {
        if (oldData.isNotEmpty()) {
            val courseSet = HashSet<Course>(oldData.size)
            val styles = ArrayList<CourseCellStyle>(oldData.size)
            var term: String? = null
            for (oldDatum in oldData) {
                if (!oldDatum.courseId.isNullOrEmpty() && !oldDatum.courseName.isNullOrEmpty()) {
                    val courseId = oldDatum.courseId!!.trim()
                    val fixedCourseId =
                        if (courseId.startsWith("Custom")) {
                            CourseUtils.getNewCourseId()
                        } else {
                            courseId
                        }
                    val timeSet = convertOldDataToCourseTime(context, fixedCourseId, oldDatum.courseDetail!!)
                    courseSet.add(
                        Course(
                            fixedCourseId,
                            oldDatum.courseName!!.trim(),
                            oldDatum.courseTeacher?.trim() ?: Constants.EMPTY,
                            oldDatum.courseCombinedClass?.trim(),
                            oldDatum.courseClass?.trim() ?: Constants.EMPTY,
                            oldDatum.courseScore?.trim()?.toFloat() ?: 0f,
                            oldDatum.courseType?.trim() ?: Constants.EMPTY,
                            null,
                            timeSet
                        )
                    )
                    styles.add(CourseCellStyle.getDefaultCellStyle(courseId, oldDatum.courseColor))
                    if (oldDatum.courseTerm != null) term = oldDatum.courseTerm.trim()
                }
            }
            val newTerm =
                if (term == null) {
                    TermDate.generateNewTermDate().getTerm()
                } else {
                    Term.parse(term)
                }
            return Pair(CourseSet(courseSet, newTerm), styles.toTypedArray())
        }
        return null
    }

    private fun convertOldDataToCourseTime(context: Context, id: String, oldData: Array<CourseDetailCompat>?) =
        if (oldData != null) {
            HashSet<CourseTime>(oldData.size).apply {
                oldData.forEach {
                    val weeks = TimePeriodList(Array(it.weeks?.size ?: 0) { i ->
                        TimePeriod.parse(it.weeks!![i])
                    })
                    val courses = TimePeriodList(Array(it.courseTime?.size ?: 0) { i ->
                        TimePeriod.parse(it.courseTime!![i])
                    })
                    add(
                        CourseTime(
                            context,
                            id,
                            it.location ?: Constants.EMPTY,
                            /*
                                单周 COURSE_DETAIL_WEEKMODE_SINGLE = 1
                                双周 COURSE_DETAIL_WEEKMODE_DOUBLE = 2
                                仅一周 COURSE_DETAIL_WEEKMODE_ONCE = 3
                                多个连续的间断周或多个间断周 COURSE_DETAIL_WEEKMODE_ONCE_MORE = 4
                             */
                            when (it.weekMode) {
                                1 -> WeekMode.ODD_WEEK_ONLY
                                2 -> WeekMode.EVEN_WEEK_ONLY
                                else -> WeekMode.ALL_WEEKS
                            },
                            weeks,
                            it.weekDay.toShort(),
                            courses
                        )
                    )
                }
            }
        } else {
            HashSet()
        }

    private fun readLoginInfoFromOld(): LoginInfo? {
        try {
            PreferenceManager.getDefaultSharedPreferences(App.instance).apply {
                val userId = getString("USER_ID", null)
                val userPw = getString("USER_PW", null)
                if (userId != null && userPw != null) {
                    AESCompat.decrypt(userPw, userId)?.let {
                        return LoginInfo(userId, it)
                    }
                }
            }
        } catch (e: Exception) {
            ExceptionUtils.printStackTrace<OldDataCompat>(e)
            LogUtils.w<OldDataCompat>("User Info Compat Failed!")
        }
        LogUtils.w<OldDataCompat>("User Info Compat Empty!")
        return null
    }

    fun hasOldData() = PreferenceManager.getDefaultSharedPreferences(App.instance).contains("HAS_LOGIN") && File(CourseDataPath).exists()

    @SuppressLint("ApplySharedPref")
    private fun clearOldData() {
        IOUtils.deleteFile(App.instance.filesDir.absolutePath)
        IOUtils.deleteFile(App.instance.cacheDir.absolutePath)
        IOUtils.deleteFile(App.instance.codeCacheDir.absolutePath)

        App.instance.filesDir?.parentFile?.absolutePath?.let {
            IOUtils.deleteFile(it + File.separator + "shared_prefs" + File.separator + "CookiesPrefs.xml")
            IOUtils.deleteFile(it + File.separator + "shared_prefs" + File.separator + "Cookies_Prefs.xml")
            IOUtils.deleteFile(it + File.separator + "shared_prefs" + File.separator + "tool.xfy9326.naucourse_preferences.xml")
        }
    }
}