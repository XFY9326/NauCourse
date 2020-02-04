package tool.xfy9326.naucourses

import java.text.SimpleDateFormat
import java.util.*

object Constants {
    const val EMPTY = ""
    const val SPACE = " "

    object News {
        const val NEWS_STORE_DAY_LENGTH = 180
    }

    object Network {
        const val HTTP = "http"
        const val HTTP_PORT = 80
        const val HTTPS = "https"
        const val HTTPS_PORT = 443

        const val HTTP_FORBIDDEN_STATUS = 403

        const val HEADER_REFERER = "Referer"

        const val URL_QUERY_DIVIDE_SYMBOL = "?"

        const val NAU_HOST = "www.nau.edu.cn"
        const val JW_HOST = "jw.nau.edu.cn"
    }

    object HTML {
        const val ELEMENT_TAG_TD = "td"
        const val ELEMENT_TAG_TR = "tr"
        const val ELEMENT_TAG_TH = "th"
        const val ELEMENT_TAG_A = "a"
        const val ELEMENT_TAG_SPAN = "span"
        const val ELEMENT_TAG_IMG = "img"
        const val ELEMENT_TAG_TABLE = "table"
        const val ELEMENT_TAG_DIV = "div"

        const val ELEMENT_CLASS_TD_TITLE = "tdTitle"

        const val ELEMENT_ID_CONTENT = "content"

        const val ELEMENT_ATTR_HREF = "href"
        const val ELEMENT_ATTR_ID = "id"
        const val ELEMENT_ATTR_SRC = "src"
        const val ELEMENT_ATTR_CLASS = "class"
        const val ELEMENT_ATTR_VALUE = "value"
        const val ELEMENT_ATTR_TITLE = "title"

        const val SELECT_A_HREF_PATH_URL = "$ELEMENT_TAG_A[$ELEMENT_ATTR_HREF]"
        const val SELECT_IMG_PATH = "$ELEMENT_TAG_IMG[$ELEMENT_ATTR_SRC]"

        const val HTML_IMG_TAG_REG = "<$ELEMENT_TAG_IMG.*?/?>"
    }

    object Time {
        val DATE_FORMAT_YMD = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
        val DATE_FORMAT_YMD_HM_CH = SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.CHINA)

        const val MIN_WEEK_DAY = 0
        const val MAX_WEEK_DAY = 6
    }

    object Course {
        const val MIN_WEEKS_SIZE = 1
        const val MAX_WEEKS_SIZE = 24
        const val MIN_COURSE_LENGTH = 1
        const val MAX_COURSE_LENGTH = 13
    }

    object DB {
        const val SQL_LITE_TABLE = "sqlite_sequence"

        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"

        const val DEFAULT_ID = 0
    }
}