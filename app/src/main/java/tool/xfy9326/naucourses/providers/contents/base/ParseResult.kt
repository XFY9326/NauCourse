package tool.xfy9326.naucourses.providers.contents.base

data class ParseResult<T>(
    val isParseSuccess: Boolean,
    val parseData: T? = null
)