package tool.xfy9326.naucourses.network

import tool.xfy9326.naucourses.network.clients.SimpleClient

object SimpleNetworkManager {
    private val simpleClient by lazy { SimpleClient() }

    fun getClient() = simpleClient
}