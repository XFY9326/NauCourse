package tool.xfy9326.naucourses.providers.contents.base

data class RequestResult(
    val isRequestSuccess: Boolean,
    val requestContentErrorResult: ContentErrorReason = ContentErrorReason.NONE,
    val contentData: String? = null
)