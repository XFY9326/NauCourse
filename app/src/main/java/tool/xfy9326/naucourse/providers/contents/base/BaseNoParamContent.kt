package tool.xfy9326.naucourse.providers.contents.base

abstract class BaseNoParamContent<T> : BaseContent<T>() {
    @Synchronized
    fun getContentData(): ContentResult<T> = requestAndParse()
}