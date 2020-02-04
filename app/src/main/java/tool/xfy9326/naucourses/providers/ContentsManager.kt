package tool.xfy9326.naucourses.providers

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import tool.xfy9326.naucourses.providers.contents.base.BaseContent
import tool.xfy9326.naucourses.providers.contents.base.BaseNoParamContent
import tool.xfy9326.naucourses.providers.contents.base.BaseParamContent
import tool.xfy9326.naucourses.providers.contents.base.ContentErrorReason
import java.util.*

object ContentsManager {
    private val contentScope = MainScope()
    private val contentQueue: Queue<BaseContent<*>> = LinkedList<BaseContent<*>>()

    fun <T> requestNoParamContent(content: BaseNoParamContent<T>, listener: ContentListener<T>) {
        contentScope.launch {
            val defer = async { content.getContentData() }
            val result = defer.await()
            if (result.isSuccess) {
                listener.onSuccess(result.contentData!!)
            } else {
                listener.onFailed(result.contentErrorResult)
            }
        }
    }

    fun <T, E> requestParamContent(param: E, content: BaseParamContent<T, E>, listener: ContentListener<T>) {
        contentScope.launch {
            val defer = async { content.getContentData(param) }
            val result = defer.await()
            if (result.isSuccess) {
                listener.onSuccess(result.contentData!!)
            } else {
                listener.onFailed(result.contentErrorResult)
            }
        }
    }

    fun cancel() {
        contentScope.cancel()
    }

    interface ContentListener<T> {
        fun onSuccess(responseData: T)

        fun onFailed(result: ContentErrorReason)
    }
}