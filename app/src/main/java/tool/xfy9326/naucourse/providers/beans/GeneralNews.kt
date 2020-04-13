package tool.xfy9326.naucourse.providers.beans

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import okhttp3.HttpUrl
import tool.xfy9326.naucourse.Constants
import tool.xfy9326.naucourse.io.db.NewsDBHelper
import java.util.*
import kotlin.collections.HashSet

@Entity(tableName = NewsDBHelper.NEWS_TABLE_NAME)
data class GeneralNews(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = Constants.DB.COLUMN_ID)
    var id: Int,
    val title: String,
    @ColumnInfo(name = NewsDBHelper.COLUMN_POST_DATE)
    val postDate: Date,
    val detailUrl: HttpUrl,
    val type: String?,
    val postSource: PostSource,
    val clickAmount: Int?
) {
    // For RSSObject
    constructor(title: String, postDate: Date, detailUrl: HttpUrl, type: String?, postSource: PostSource) :
            this(Constants.DB.DEFAULT_ID, title, postDate, detailUrl, type, postSource, null)

    // For AlstuMessage
    constructor(title: String, postDate: Date, detailUrl: HttpUrl, postSource: PostSource) :
            this(Constants.DB.DEFAULT_ID, title, postDate, detailUrl, null, postSource, null)

    // For JwcTopic
    constructor(title: String, postDate: Date, detailUrl: HttpUrl, type: String, postSource: PostSource, clickAmount: Int) :
            this(Constants.DB.DEFAULT_ID, title, postDate, detailUrl, type, postSource, clickAmount)


    enum class PostSource {
        UNKNOWN, JWC, ALSTU, RSS_JW, RSS_TW, RSS_XGC, RSS_XXB
    }

    companion object {
        @Suppress("unused")
        fun convertPostSourceSetToStringSet(set: Set<PostSource>): Set<String> {
            val result = HashSet<String>(set.size)
            for (postSource in set) {
                result.add(postSource.name)
            }
            return result
        }

        fun parseStringSet(set: Set<String>, deleteUnknownSource: Boolean = false): Set<PostSource> {
            val result = HashSet<PostSource>(set.size)
            for (s in set) {
                try {
                    result.add(PostSource.valueOf(s))
                } catch (e: IllegalArgumentException) {
                    if (!deleteUnknownSource) {
                        result.add(PostSource.UNKNOWN)
                    }
                }
            }
            return result
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GeneralNews

        if (title != other.title) return false
        if (postDate != other.postDate) return false
        if (detailUrl != other.detailUrl) return false
        if (type != other.type) return false
        if (postSource != other.postSource) return false
        if (clickAmount != other.clickAmount) return false

        return true
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + postDate.hashCode()
        result = 31 * result + detailUrl.hashCode()
        result = 31 * result + (type?.hashCode() ?: 0)
        result = 31 * result + postSource.hashCode()
        result = 31 * result + (clickAmount ?: 0)
        return result
    }
}