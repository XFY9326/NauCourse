package tool.xfy9326.naucourses.providers.contents.beans.rss

data class RSSObject(val channels: Array<RSSChannel>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RSSObject

        if (!channels.contentEquals(other.channels)) return false

        return true
    }

    override fun hashCode(): Int {
        return channels.contentHashCode()
    }
}