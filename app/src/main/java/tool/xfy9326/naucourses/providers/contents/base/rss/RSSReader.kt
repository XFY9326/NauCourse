package tool.xfy9326.naucourses.providers.contents.base.rss

import android.util.Xml
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.xmlpull.v1.XmlPullParser
import tool.xfy9326.naucourses.providers.beans.rss.RSSChannel
import tool.xfy9326.naucourses.providers.beans.rss.RSSItem
import tool.xfy9326.naucourses.providers.beans.rss.RSSObject
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

object RSSReader {
    private const val RSS_TAG_CHANNEL = "channel"
    private const val RSS_TAG_TITLE = "title"
    private const val RSS_TAG_LINK = "link"
    private const val RSS_TAG_ITEM = "item"
    // private const val RSS_TAG_DESCRIPTION = "description"
    private const val RSS_TAG_DATE = "date"
    // private const val RSS_TAG_TYPE = "type"
    private const val RSS_TAG = "rss"

    private const val RSS_TYPE_START_STR_1 = "【"
    private const val RSS_TYPE_END_STR_1 = "】"
    private const val RSS_TYPE_START_STR_2 = "["
    private const val RSS_TYPE_END_STR_2 = "]"

    private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.CHINA)

    fun getRSSObject(rssContent: String): RSSObject? {
        val parser = Xml.newPullParser()
        val stringReader = StringReader(rssContent)
        parser.setInput(stringReader)

        var eventType = parser.eventType
        var rssObject: RSSObject? = null
        val rssChannelArrayList: ArrayList<RSSChannel> = ArrayList()
        val rssItemArrayList: ArrayList<RSSItem> = ArrayList()
        var startChannel = false
        var startItem = false
        var title: String? = null
        var link: HttpUrl? = null
        var date: Date? = null
        var type: String? = null
        var channelTitle: String? = null
        var channelLink: HttpUrl? = null

        while (eventType != XmlPullParser.END_DOCUMENT) {
            val tagName = parser.name
            if (eventType == XmlPullParser.START_TAG) {
                when (tagName) {
                    RSS_TAG_CHANNEL -> startChannel = true
                    RSS_TAG_ITEM -> startItem = true
                    RSS_TAG_TITLE -> {
                        if (startChannel && !startItem) {
                            channelTitle = parser.nextText()
                        } else {
                            title = parser.nextText().trim()
                            if (title.startsWith(RSS_TYPE_START_STR_1)) {
                                type = title.substring(1, title.indexOf(RSS_TYPE_END_STR_1))
                                title = title.substring(title.indexOf(RSS_TYPE_END_STR_1) + 1)
                            } else if (title.startsWith(RSS_TYPE_START_STR_2)) {
                                type = title.substring(1, title.indexOf(RSS_TYPE_END_STR_2))
                                title = title.substring(title.indexOf(RSS_TYPE_END_STR_2) + 1)
                            }
                        }
                    }
                    RSS_TAG_LINK -> {
                        if (startChannel && !startItem) {
                            channelLink = parser.nextText().toHttpUrl()
                        } else {
                            link = parser.nextText().toHttpUrl()
                        }
                    }
                    RSS_TAG_DATE -> {
                        date = readTime(parser.nextText())
                    }
                }
            } else if (eventType == XmlPullParser.END_TAG) {
                when (tagName) {
                    RSS_TAG_CHANNEL -> if (startChannel) {
                        startChannel = false
                        rssChannelArrayList.add(
                            RSSChannel(
                                channelTitle!!,
                                channelLink!!,
                                rssItemArrayList.toTypedArray()
                            )
                        )
                        channelTitle = null
                        channelLink = null
                        rssItemArrayList.clear()
                    }
                    RSS_TAG_ITEM -> if (startItem) {
                        startItem = false
                        rssItemArrayList.add(
                            RSSItem(
                                title!!,
                                link!!,
                                date!!,
                                type
                            )
                        )
                        title = null
                        link = null
                        date = null
                        type = null
                    }
                    RSS_TAG -> {
                        rssObject =
                            RSSObject(rssChannelArrayList.toTypedArray())
                        rssChannelArrayList.clear()
                    }
                }
            }
            eventType = parser.next()
        }
        return rssObject
    }

    // 解决SimpleDateFormat线程不安全问题
    @Synchronized
    private fun readTime(text: String) = DATE_FORMAT.parse(text)
}