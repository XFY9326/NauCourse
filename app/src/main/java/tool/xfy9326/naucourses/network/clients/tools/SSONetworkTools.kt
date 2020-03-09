package tool.xfy9326.naucourses.network.clients.tools

import okhttp3.*
import tool.xfy9326.naucourses.network.clients.utils.CookieStore
import tool.xfy9326.naucourses.network.clients.utils.UAInterceptor
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.TimeUnit

class SSONetworkTools private constructor() {

    companion object {
        @Volatile
        lateinit var instance: SSONetworkTools

        @Volatile
        private var cookieStore: CookieStore? = null

        @Volatile
        private lateinit var okhttpClient: OkHttpClient

        lateinit var cacheDir: File

        // TimeUnit.SECONDS
        private const val CONNECT_TIME_OUT = 7L

        // TimeUnit.SECONDS
        private const val READ_TIME_OUT = 8L

        // TimeUnit.SECONDS
        private const val WRITE_TIME_OUT = 8L

        private const val MAX_CONNECTION_NUM = 15

        // TimeUnit.MINUTES
        private const val ALIVE_CONNECTION_NUM = 5L

        private const val CACHE_DIR = "SSONetworkCache"
        private const val CACHE_SIZE = 1024 * 1024 * 10L

        fun initInstance(cacheParentDir: String) = synchronized(this) {
            if (!::instance.isInitialized) {
                instance = SSONetworkTools()
                if (!::okhttpClient.isInitialized) {
                    cacheDir = File(cacheParentDir + File.separator + CACHE_DIR)
                    createClient(cookieStore ?: createCookieStore()).also { okhttpClient = it }
                }
            }
        }

        private fun createClient(cookieStore: CookieStore): OkHttpClient {
            return OkHttpClient.Builder().apply {
                connectTimeout(CONNECT_TIME_OUT, TimeUnit.SECONDS)
                readTimeout(READ_TIME_OUT, TimeUnit.SECONDS)
                writeTimeout(WRITE_TIME_OUT, TimeUnit.SECONDS)
                retryOnConnectionFailure(true)
                cookieJar(cookieStore)
                followRedirects(true)
                followSslRedirects(true)
                cache(Cache(cacheDir, CACHE_SIZE))
                connectionPool(
                    ConnectionPool(
                        MAX_CONNECTION_NUM,
                        ALIVE_CONNECTION_NUM,
                        TimeUnit.MINUTES
                    )
                )
                addInterceptor(UAInterceptor())
            }.build()
        }

        fun getResponseContent(response: Response): String {
            val body = response.body
            val contentType = body?.contentType()
            val source = body?.source()
            source!!.request(Long.MAX_VALUE)
            val buffer = source.buffer

            val bufferClone = buffer.clone()
            val result = bufferClone.readString(
                (if (contentType != null) contentType.charset(StandardCharsets.UTF_8) else StandardCharsets.UTF_8) ?: StandardCharsets.UTF_8
            )
            bufferClone.close()
            return result
        }

        private fun createCookieStore(): CookieStore =
            CookieStore()

        fun HttpUrl.hasSameHost(url: HttpUrl?): Boolean =
            url != null && this.host.toLowerCase(Locale.CHINA) == url.host.toLowerCase(Locale.CHINA)

        fun HttpUrl.hasSameHost(host: String?): Boolean =
            host != null && this.host.toLowerCase(Locale.CHINA) == host.toLowerCase(Locale.CHINA)
    }

    fun getClient(): OkHttpClient = okhttpClient

    fun getCookieStore(): CookieStore = cookieStore ?: synchronized(this) {
        cookieStore
            ?: createCookieStore().also { cookieStore = it }
    }
}