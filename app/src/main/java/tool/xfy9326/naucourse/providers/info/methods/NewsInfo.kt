package tool.xfy9326.naucourse.providers.info.methods

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import okhttp3.HttpUrl
import tool.xfy9326.naucourse.constants.OthersConst
import tool.xfy9326.naucourse.io.db.NewsDBHelper
import tool.xfy9326.naucourse.providers.beans.GeneralNews
import tool.xfy9326.naucourse.providers.beans.PostSource
import tool.xfy9326.naucourse.providers.contents.base.BaseNewsContent
import tool.xfy9326.naucourse.providers.contents.base.ContentErrorReason
import tool.xfy9326.naucourse.providers.contents.base.ContentResult
import tool.xfy9326.naucourse.providers.contents.methods.jwc.TopicList
import tool.xfy9326.naucourse.providers.contents.methods.rss.JwRSS
import tool.xfy9326.naucourse.providers.contents.methods.rss.TwRSS
import tool.xfy9326.naucourse.providers.contents.methods.rss.XgcRSS
import tool.xfy9326.naucourse.providers.contents.methods.rss.XxbRSS
import tool.xfy9326.naucourse.providers.info.base.BaseSimpleContentInfo
import tool.xfy9326.naucourse.utils.debug.ExceptionUtils

object NewsInfo : BaseSimpleContentInfo<List<GeneralNews>, PostSource>() {
    private const val INIT_PER_TYPE_NEWS_SIZE = 10

    private val CONTENT_MAP = mapOf<PostSource, BaseNewsContent<*>>(
        PostSource.JWC to TopicList,
        PostSource.RSS_JW to JwRSS,
        PostSource.RSS_TW to TwRSS,
        PostSource.RSS_XGC to XgcRSS,
        PostSource.RSS_XXB to XxbRSS
    )

    override suspend fun loadSimpleStoredInfo(): List<GeneralNews>? =
        sortNewsList(NewsDBHelper.getGeneralNewsArray())

    override fun onReadSimpleCache(data: List<GeneralNews>): List<GeneralNews> {
        return data.filterNot {
            it.postSource == PostSource.UNKNOWN
        }
    }

    override suspend fun getSimpleInfoContent(params: Set<PostSource>): ContentResult<List<GeneralNews>> {
        val resultList = HashSet<GeneralNews>(params.size * INIT_PER_TYPE_NEWS_SIZE)
        val resultDeferred = arrayOfNulls<Deferred<ContentResult<Set<GeneralNews>>?>>(params.size)
        return coroutineScope {
            for ((i, param) in params.withIndex()) {
                resultDeferred[i] =
                    async {
                        if (param == PostSource.UNKNOWN) {
                            throw IllegalArgumentException("Unknown Post Source")
                        } else {
                            try {
                                CONTENT_MAP[param]?.getContentData()!!
                            } catch (e: Exception) {
                                ExceptionUtils.printStackTrace<NewsInfo>(e)
                                null
                            }
                        }
                    }
            }

            var contentErrorReason: ContentErrorReason? = null
            for (deferred in resultDeferred) {
                val result = deferred!!.await()
                if (result?.isSuccess == true) {
                    resultList.addAll(result.contentData!!)
                } else {
                    contentErrorReason = result?.contentErrorResult ?: ContentErrorReason.OPERATION
                    break
                }
            }
            when {
                contentErrorReason != null -> ContentResult(false, contentErrorResult = contentErrorReason)
                resultList.size != 0 -> {
                    val dateStamp = isNewsOutOfDateTimeStamp()
                    val list = resultList.toList().filterNot {
                        it.postDate.time < dateStamp
                    }
                    ContentResult(true, contentData = sortNewsList(list))
                }
                else -> ContentResult(true, ContentErrorReason.EMPTY_DATA, emptyList())
            }
        }
    }

    fun isNewsOutOfDateTimeStamp() = System.currentTimeMillis() - OthersConst.NEWS_STORE_DAY_LENGTH * 24 * 60 * 60 * 1000L

    fun getDetailNewsInfo(url: HttpUrl, newsType: PostSource) =
        if (newsType == PostSource.UNKNOWN) {
            throw IllegalArgumentException("Unknown Post Source")
        } else {
            CONTENT_MAP[newsType]?.getContentDetailData(url)!!
        }

    fun getImageUrlForNewsInfo(source: String, newsType: PostSource): HttpUrl {
        if (newsType == PostSource.UNKNOWN) {
            throw IllegalArgumentException("Unknown Post Source")
        } else if (!CONTENT_MAP.containsKey(newsType)) {
            throw IllegalArgumentException("Unknown News Type")
        } else {
            return CONTENT_MAP[newsType]?.getNewsImageUrl(source)!!
        }
    }

    fun getLoginClientTypeByPostSource(postSource: PostSource) =
        if (postSource == PostSource.RSS_JW || postSource == PostSource.RSS_TW ||
            postSource == PostSource.RSS_XGC || postSource == PostSource.RSS_XXB
        ) {
            null
        } else {
            CONTENT_MAP[postSource]?.clientType
        }

    override suspend fun saveSimpleInfo(info: List<GeneralNews>) {
        NewsDBHelper.clearAll()
        NewsDBHelper.putGeneralNewsSet(info)
    }

    override suspend fun clearSimpleStoredInfo() = NewsDBHelper.clearAll()

    private fun sortNewsList(list: List<GeneralNews>): List<GeneralNews> =
        list.sortedWith(Comparator { o1, o2 ->
            val result1 = o2.postDate.time.compareTo(o1.postDate.time)
            if (result1 == 0) {
                o2.title.compareTo(o1.title)
            } else {
                result1
            }
        })
}