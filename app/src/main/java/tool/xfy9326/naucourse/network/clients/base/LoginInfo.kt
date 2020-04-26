package tool.xfy9326.naucourse.network.clients.base

import java.io.Serializable

// 登录信息
data class LoginInfo(
    val userId: String,
    val userPw: String
) : Serializable