package tool.xfy9326.naucourse.network.clients.base

import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

abstract class BaseNetworkClient {

    // 获取网络客户端
    abstract fun getNetworkClient(): OkHttpClient

    // 使用该客户端进行请求
    abstract fun newClientCall(request: Request): Response

    fun newClientCall(url: HttpUrl) = newClientCall(Request.Builder().url(url).build())
}