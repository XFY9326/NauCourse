package tool.xfy9326.naucourses.network.clients.utils

import okhttp3.Interceptor
import okhttp3.Response

class UAInterceptor : Interceptor {
    private val globalUA = UAPool.getRandomUA()

    companion object {
        private const val USER_AGENT = "User-Agent"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
//        Log.d("SSONetwork", "UA: $globalUA")
//        Log.d("SSONetwork", "Method: ${request.method} Url: ${request.url}")
//        if (request.headers.size > 0) {
//            Log.d("SSONetwork", "Header: ${request.headers}")
//        }
        val requestBuilder = request.newBuilder()
        val headerBuilder = request.headers.newBuilder()
        headerBuilder[USER_AGENT] = globalUA
        requestBuilder.headers(headerBuilder.build())
        return chain.proceed(requestBuilder.build())
//        val response = chain.proceed(requestBuilder.build())
//        Log.d("SSONetwork", "Turn To: ${response.request.url} Status: ${response.code}")
//        return response
    }
}