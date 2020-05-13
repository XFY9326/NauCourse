package tool.xfy9326.naucourse.compat

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
import tool.xfy9326.naucourse.io.db.CourseCellStyleDBHelper
import tool.xfy9326.naucourse.io.prefs.AppPref
import tool.xfy9326.naucourse.network.clients.base.LoginInfo
import tool.xfy9326.naucourse.providers.beans.jwc.*
import tool.xfy9326.naucourse.providers.info.methods.CourseInfo
import tool.xfy9326.naucourse.utils.courses.CourseStyleUtils
import tool.xfy9326.naucourse.utils.courses.CourseUtils
import tool.xfy9326.naucourse.utils.courses.TimeUtils
import tool.xfy9326.naucourse.utils.debug.ExceptionUtils
import tool.xfy9326.naucourse.utils.io.BaseIOUtils
import tool.xfy9326.naucourse.utils.io.TextIOUtils
import tool.xfy9326.naucourse.utils.secure.AccountUtils
import java.io.File

// 旧版本数据适配
object OldDataCompat {
    private val CourseDataPath = App.instance.filesDir.absolutePath + File.separator + "Course.txn"

    // 旧版本数据转移
    suspend fun applyCompatDataToCurrentStore() = withContext(Dispatchers.IO) {
        val loginInfo = readLoginInfoFromOld()
        val courseData =
            if (loginInfo != null) {
                readCourseDataFromOld(loginInfo.userId)
            } else {
                null
            }
        val showHiddenFunction = readHiddenFunctionConfigFromOld()

        clearOldData()

        AppPref.EnableAdvancedFunctions = showHiddenFunction

        if (loginInfo != null) {
            AccountUtils.saveUserInfo(UserInfo(loginInfo.userId, loginInfo.userPw))
        }

        if (courseData != null) {
            CourseInfo.saveNewCourses(courseData.first)
            CourseCellStyleDBHelper.saveCourseCellStyle(courseData.second)
        }

        return@withContext loginInfo
    }

    // 从旧版本数据中读取课程数据
    private suspend fun readCourseDataFromOld(userId: String): Pair<CourseSet, Array<CourseCellStyle>>? {
        try {
            val text = TextIOUtils.readTextFromFile(CourseDataPath)
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
        }
        return null
    }

    // 将旧版本课程数据转为当前版本的数据
    private fun convertOldDataToCourseSet(context: Context, oldData: ArrayList<CourseCompat>): Pair<CourseSet, Array<CourseCellStyle>>? {
        if (oldData.isNotEmpty()) {
            val courseSet = HashSet<Course>(oldData.size)
            val styles = ArrayList<CourseCellStyle>(oldData.size)
            var term: String? = null
            for (oldDatum in oldData) {
                try {
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
                                oldDatum.courseScore?.trim()?.toFloatOrNull() ?: 0f,
                                oldDatum.courseType?.trim() ?: Constants.EMPTY,
                                null,
                                timeSet
                            )
                        )
                        styles.add(CourseStyleUtils.getDefaultCellStyle(courseId, oldDatum.courseColor))
                        if (oldDatum.courseTerm != null) term = oldDatum.courseTerm.trim()
                    }
                } catch (e: Exception) {
                    ExceptionUtils.printStackTrace<OldDataCompat>(e)
                }
            }
            val newTerm =
                if (term == null) {
                    TimeUtils.generateNewTermDate().getTerm()
                } else {
                    Term.parse(term)
                }
            return Pair(CourseSet(courseSet, newTerm), styles.toTypedArray())
        }
        return null
    }

    // 将旧版本课程数据转为当前版本的课程时间数据
    private fun convertOldDataToCourseTime(context: Context, id: String, oldData: Array<CourseDetailCompat>?) =
        if (oldData != null) {
            HashSet<CourseTime>(oldData.size).apply {
                oldData.forEach {
                    val weeks = TimePeriodList(Array(it.weeks?.size ?: 0) { i ->
                        TimePeriod.parse(it.weeks!![i])
                    })
                    val courses = Array(it.courseTime?.size ?: 0) { i ->
                        TimePeriod.parse(it.courseTime!![i])
                    }
                    for (period in courses) {
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
                                TimePeriodList(period)
                            )
                        )
                    }
                }
            }
        } else {
            HashSet()
        }

    // 从旧版本读取登录信息
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
        }
        return null
    }

    // 从旧版本读取高级功能是否启用
    private fun readHiddenFunctionConfigFromOld(): Boolean {
        PreferenceManager.getDefaultSharedPreferences(App.instance).apply {
            return getBoolean("SHOW_HIDDEN_FUNCTION", false)
        }
    }

    // 判断是否存在旧版本数据
    fun hasOldData() = PreferenceManager.getDefaultSharedPreferences(App.instance).contains("HAS_LOGIN") && File(CourseDataPath).exists()

    // 清空旧版本数据
    private fun clearOldData() {
        BaseIOUtils.deleteFile(App.instance.filesDir.absolutePath)
        BaseIOUtils.deleteFile(App.instance.cacheDir.absolutePath)
        BaseIOUtils.deleteFile(App.instance.codeCacheDir.absolutePath)

        App.instance.filesDir?.parentFile?.absolutePath?.let {
            BaseIOUtils.deleteFile(it + File.separator + "shared_prefs" + File.separator + "CookiesPrefs.xml")
            BaseIOUtils.deleteFile(it + File.separator + "shared_prefs" + File.separator + "Cookies_Prefs.xml")
            BaseIOUtils.deleteFile(it + File.separator + "shared_prefs" + File.separator + "tool.xfy9326.naucourse_preferences.xml")
        }
    }
}