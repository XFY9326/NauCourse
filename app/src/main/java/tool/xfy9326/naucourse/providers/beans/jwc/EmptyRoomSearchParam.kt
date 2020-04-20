package tool.xfy9326.naucourse.providers.beans.jwc

import java.util.*

data class EmptyRoomSearchParam(
    val campusName: String,
    val term: String,
    val searchDate: Date,
    val startDate: Date,
    val endDate: Date,
    val BJC: Int,
    val EJC: Int
)