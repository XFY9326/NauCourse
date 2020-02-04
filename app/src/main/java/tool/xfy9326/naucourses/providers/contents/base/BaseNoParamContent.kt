package tool.xfy9326.naucourses.providers.contents.base

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

abstract class BaseNoParamContent<T> : BaseContent<T>() {
    @Synchronized
    suspend fun getContentData(): ContentResult<T> = withContext(Dispatchers.IO) {
        requestAndParse()
    }
}