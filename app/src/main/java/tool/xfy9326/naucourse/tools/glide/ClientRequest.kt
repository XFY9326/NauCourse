package tool.xfy9326.naucourse.tools.glide

import tool.xfy9326.naucourse.network.LoginNetworkManager
import java.io.Serializable

data class ClientRequest(
    val clientType: LoginNetworkManager.ClientType,
    val url: String
) : Serializable