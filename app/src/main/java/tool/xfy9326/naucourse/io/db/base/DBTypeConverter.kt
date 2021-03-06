package tool.xfy9326.naucourse.io.db.base

import androidx.room.TypeConverter
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import tool.xfy9326.naucourse.providers.beans.PostSource
import tool.xfy9326.naucourse.providers.beans.jwc.Term
import tool.xfy9326.naucourse.providers.beans.jwc.TimePeriodList
import tool.xfy9326.naucourse.providers.beans.jwc.WeekMode
import java.util.*

// 数据库类型存储转换
class DBTypeConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? = value?.let { Date(it) }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? = date?.time

    @TypeConverter
    fun fromUrl(value: String?): HttpUrl? = value?.toHttpUrl()

    @TypeConverter
    fun httpUrlToUrl(httpUrl: HttpUrl?): String? = httpUrl?.toString()

    @TypeConverter
    fun postSourceToPostSourceName(postSource: PostSource): String = postSource.name

    @TypeConverter
    fun fromPostSourceName(value: String): PostSource = try {
        PostSource.valueOf(value)
    } catch (e: IllegalArgumentException) {
        PostSource.UNKNOWN
    }

    @TypeConverter
    fun fromTermString(value: String?): Term? = value?.let { Term.parse(it) }

    @TypeConverter
    fun termToTermString(term: Term?): String? = term?.toString()

    @TypeConverter
    fun timePeriodListToTimePeriodListString(timePeriodList: TimePeriodList?): String? = timePeriodList?.toString()

    @TypeConverter
    fun fromTimePeriodListString(value: String?): TimePeriodList? = value?.let { TimePeriodList.parse(it) }

    @TypeConverter
    fun weekModeToWeekModeName(weekMode: WeekMode): String = weekMode.name

    @TypeConverter
    fun fromWeekModeName(value: String): WeekMode = try {
        WeekMode.valueOf(value)
    } catch (e: IllegalArgumentException) {
        WeekMode.ALL_WEEKS
    }
}