package tool.xfy9326.naucourse.providers.contents.base

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import tool.xfy9326.naucourse.constants.HTMLConst
import tool.xfy9326.naucourse.constants.NetworkConst
import tool.xfy9326.naucourse.providers.beans.GeneralNews
import tool.xfy9326.naucourse.providers.beans.GeneralNewsDetail
import tool.xfy9326.naucourse.utils.debug.ExceptionUtils

abstract class BaseNewsContent<T> : BaseNoParamContent<Set<GeneralNews>>() {
    protected open fun getDetailNetworkClient() = networkClient

    protected abstract fun convertToGeneralNews(newsData: Set<T>): Set<GeneralNews>

    protected abstract fun onParseRawData(content: String): Set<T>

    private fun requestDetailData(url: HttpUrl): RequestResult = requestData { onRequestDetailData(url) }

    protected abstract fun onRequestDetailData(url: HttpUrl): Response

    protected open fun onBuildImageUrl(source: String): HttpUrl = source.toHttpUrl()

    fun getNewsImageUrl(source: String) =
        if (source.startsWith(NetworkConst.HTTP)) {
            source.toHttpUrl()
        } else {
            onBuildImageUrl(source)
        }

    protected abstract fun onParseDetailData(document: Document): GeneralNewsDetail

    private fun parseDetailData(contentData: String): ParseResult<GeneralNewsDetail> = try {
        val document = Jsoup.parse(contentData)
        imgTagFormat(document)
        ParseResult(true, onParseDetailData(document))
    } catch (e: Exception) {
        ExceptionUtils.printStackTrace<BaseNewsContent<T>>(e)
        ParseResult(false)
    }

    private fun imgTagFormat(document: Document) {
        val imgTag = document.body()?.getElementsByTag(HTMLConst.ELEMENT_TAG_IMG)
        if (imgTag != null) {
            for (element in imgTag) {
                val src = element.attr("src")
                element.clearAttributes().attr("src", src)
                element.previousElementSibling()?.appendElement(HTMLConst.ELEMENT_TAG_BR)
                element.appendElement(HTMLConst.ELEMENT_TAG_BR)
            }
        }
    }

    final override fun onParseData(content: String): Set<GeneralNews> = convertToGeneralNews(onParseRawData(content))

    @Synchronized
    fun getContentDetailData(url: HttpUrl): ContentResult<GeneralNewsDetail> = requestAndParse(requestDetailData(url)) {
        parseDetailData(it)
    }
}