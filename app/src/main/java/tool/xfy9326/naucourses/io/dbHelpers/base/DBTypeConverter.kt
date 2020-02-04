package tool.xfy9326.naucourses.io.dbHelpers.base

import androidx.room.TypeConverter
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import tool.xfy9326.naucourses.providers.contents.beans.GeneralNews
import tool.xfy9326.naucourses.providers.contents.beans.jwc.Term
import java.util.*

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
    fun fromPostSourceName(value: String): GeneralNews.PostSource = try {
        GeneralNews.PostSource.valueOf(value)
    } catch (e: IllegalArgumentException) {
        GeneralNews.PostSource.UNKNOWN
    }

    @TypeConverter
    fun fromTermString(value: String?): Term? = value?.let { Term.parse(it) }

    @TypeConverter
    fun termToTermString(term: Term?): String? = term?.toString()

    @TypeConverter
    fun postSourceToPostSourceName(postSource: GeneralNews.PostSource): String = postSource.name
}