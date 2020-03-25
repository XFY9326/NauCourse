package tool.xfy9326.naucourse.network.clients

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import tool.xfy9326.naucourse.network.clients.base.BaseNetworkClient

class SimpleClient : BaseNetworkClient() {
    private val okHttpClient = OkHttpClient()

    override fun getNetworkClient(): OkHttpClient = okHttpClient

    override fun newClientCall(request: Request): Response = okHttpClient.newCall(request).execute()
}