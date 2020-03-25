package tool.xfy9326.naucourse.beans

import tool.xfy9326.naucourse.network.clients.base.LoginInfo

data class UserInfo(
    val userId: String,
    val userPw: String
) {
    fun toLoginInfo() = LoginInfo(userId, userPw)
}