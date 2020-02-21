package tool.xfy9326.naucourses.providers.contents.base

abstract class BaseParamContent<T, E> : BaseContent<T>() {

    protected abstract fun onParamSet(param: E)

    @Synchronized
    fun getContentData(param: E): ContentResult<T> {
        onParamSet(param)
        return requestAndParse()
    }
}