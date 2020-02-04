package tool.xfy9326.naucourses.network.clients.base

import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

abstract class BaseNetworkClient {

    abstract fun getNetworkClient(): OkHttpClient

    /**
     * 使用该客户端进行请求
     * @param request 请求
     * @return 响应
     */
    abstract fun newClientCall(request: Request): Response

    fun newClientCall(url: HttpUrl) = newClientCall(Request.Builder().url(url).build())
}