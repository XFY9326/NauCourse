package tool.xfy9326.naucourses.providers.beans

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import okhttp3.HttpUrl
import tool.xfy9326.naucourses.Constants
import tool.xfy9326.naucourses.io.dbHelpers.AppDBHelper
import java.util.*

@Entity(tableName = AppDBHelper.NEWS_TABLE_NAME)
data class GeneralNews(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = AppDBHelper.COLUMN_ID)
    var id: Int,
    val title: String,
    @ColumnInfo(name = AppDBHelper.COLUMN_POST_DATE)
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GeneralNews

        if (title != other.title) return false
        if (postDate != other.postDate) return false
        if (detailUrl != other.detailUrl) return false
        if (type != other.type) return false
        if (postSource != other.postSource) return false

        return true
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + postDate.hashCode()
        result = 31 * result + detailUrl.hashCode()
        result = 31 * result + (type?.hashCode() ?: 0)
        result = 31 * result + postSource.name.hashCode()
        return result
    }

}