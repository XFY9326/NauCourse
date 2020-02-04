package tool.xfy9326.naucourses.network

import tool.xfy9326.naucourses.network.clients.SimpleClient

class SimpleNetworkManager {
    private val simpleClient by lazy { SimpleClient() }

    companion object {
        @Volatile
        private lateinit var instance: SimpleNetworkManager

        fun getInstance(): SimpleNetworkManager = synchronized(this) {
            if (!::instance.isInitialized) {
                instance = SimpleNetworkManager()
            }
            return instance
        }
    }

    fun getClient() = simpleClient
}