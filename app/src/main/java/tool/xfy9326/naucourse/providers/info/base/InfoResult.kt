package tool.xfy9326.naucourse.providers.info.base

import tool.xfy9326.naucourse.providers.contents.base.ContentErrorReason

data class InfoResult<T>(
    val isSuccess: Boolean,
    val data: T? = null,
    val errorReason: ContentErrorReason = ContentErrorReason.NONE
)