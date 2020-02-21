package tool.xfy9326.naucourses.providers.info.base

import tool.xfy9326.naucourses.providers.contents.base.ContentErrorReason

data class InfoResult<T>(
    val isSuccess: Boolean,
    val data: T? = null,
    val errorReason: ContentErrorReason = ContentErrorReason.NONE
)