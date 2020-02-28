package tool.xfy9326.naucourses.providers.contents.base

enum class ContentErrorReason {
    NONE,
    TIMEOUT,
    SERVER_ERROR,
    OPERATION,
    PARSE_FAILED,
    EMPTY_DATA,
    CONNECTION_ERROR,
    UNKNOWN
}