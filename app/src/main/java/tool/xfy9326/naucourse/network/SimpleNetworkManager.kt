package tool.xfy9326.naucourse.network

import tool.xfy9326.naucourse.network.clients.SimpleClient

object SimpleNetworkManager {
    private val simpleClient by lazy { SimpleClient() }

    fun getClient() = simpleClient
}