package tool.xfy9326.naucourses.providers.contents.base

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

abstract class BaseParamContent<T, E> : BaseContent<T>() {

    protected abstract fun onParamSet(param: E)

    @Synchronized
    suspend fun getContentData(param: E): ContentResult<T> = withContext(Dispatchers.IO) {
        onParamSet(param)
        requestAndParse()
    }
}