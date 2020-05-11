package tool.xfy9326.naucourse.providers.contents.base

enum class ContentErrorReason {
    NONE,
    TIMEOUT,
    SERVER_ERROR,
    OPERATION,
    PARSE_FAILED,
    EMPTY_DATA,
    DATA_ERROR,
    CONNECTION_ERROR,
    UNKNOWN
}