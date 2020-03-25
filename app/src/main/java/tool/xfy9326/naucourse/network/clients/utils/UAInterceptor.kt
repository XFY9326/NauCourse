package tool.xfy9326.naucourse.network.clients.utils

import okhttp3.Interceptor
import okhttp3.Response

class UAInterceptor : Interceptor {
    private val globalUA = UAPool.getRandomUA()

    companion object {
        private const val USER_AGENT = "User-Agent"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestBuilder = request.newBuilder()
        val headerBuilder = request.headers.newBuilder()
        headerBuilder[USER_AGENT] = globalUA
        requestBuilder.headers(headerBuilder.build())
        return chain.proceed(requestBuilder.build())
    }
}