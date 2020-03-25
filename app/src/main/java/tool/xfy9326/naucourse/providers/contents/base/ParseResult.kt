package tool.xfy9326.naucourse.providers.contents.base

data class ParseResult<T>(
    val isParseSuccess: Boolean,
    val parseData: T? = null
)