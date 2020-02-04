package tool.xfy9326.naucourses.providers.contents.base

data class ContentResult<T>(
    val isSuccess: Boolean,
    val contentErrorResult: ContentErrorReason = ContentErrorReason.NONE,
    val contentData: T? = null
)