package tool.xfy9326.naucourse.beans

import tool.xfy9326.naucourse.network.clients.base.LoginInfo

// 用户信息
data class UserInfo(
    // 用户ID
    val userId: String,
    // 用户密码
    val userPw: String
) {
    // 转为登录信息
    fun toLoginInfo() = LoginInfo(userId, userPw)
}