package tool.xfy9326.naucourse.beans

import okhttp3.HttpUrl

// 选择校历的单项
data class CalendarItem(
    // 校历名称
    val name: String,
    // 校历网页地址（非图片地址）
    val url: HttpUrl
)