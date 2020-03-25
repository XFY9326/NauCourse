package tool.xfy9326.naucourse.providers.beans.rss

import okhttp3.HttpUrl

data class RSSChannel(val title: String, val link: HttpUrl, val items: Array<RSSItem>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RSSChannel

        if (title != other.title) return false
        if (link != other.link) return false
        if (!items.contentEquals(other.items)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + link.hashCode()
        result = 31 * result + items.contentHashCode()
        return result
    }
}