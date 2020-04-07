package tool.xfy9326.naucourse.network.tools

import okhttp3.*
import tool.xfy9326.naucourse.App
import tool.xfy9326.naucourse.io.db.NetworkDBHelper
import tool.xfy9326.naucourse.network.utils.CookieStore
import tool.xfy9326.naucourse.network.utils.UAInterceptor
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap

class NetworkTools private constructor() {

    companion object {
        @Volatile
        private lateinit var instance_: NetworkTools

        private val cookieStoreMap = HashMap<NetworkType, CookieStore>()

        private val okhttpClientMap = HashMap<NetworkType, OkHttpClient>()

        private const val CACHE_DIR = "NetworkCache"
        private const val CACHE_SIZE = 1024 * 1024 * 10L

        val cacheDir = File(App.instance.cacheDir.absolutePath + File.separator + CACHE_DIR)

        // TimeUnit.SECONDS
        private const val CONNECT_TIME_OUT = 7L

        // TimeUnit.SECONDS
        private const val READ_TIME_OUT = 8L

        // TimeUnit.SECONDS
        private const val WRITE_TIME_OUT = 8L

        private const val MAX_CONNECTION_NUM = 15

        // TimeUnit.MINUTES
        private const val ALIVE_CONNECTION_NUM = 5L

        fun getInstance(): NetworkTools = synchronized(this) {
            if (!::instance_.isInitialized) {
                instance_ = NetworkTools()
            }
            return instance_
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

        private fun createCookieStore(type: NetworkType): CookieStore =
            when (type) {
                NetworkType.SSO -> CookieStore(NetworkDBHelper.CookiesType.SSO)
                NetworkType.NGX -> CookieStore(NetworkDBHelper.CookiesType.NGX)
            }

        fun HttpUrl.hasSameHost(url: HttpUrl?): Boolean =
            url != null && this.host.toLowerCase(Locale.CHINA) == url.host.toLowerCase(Locale.CHINA)

        fun HttpUrl.hasSameHost(host: String?): Boolean =
            host != null && this.host.toLowerCase(Locale.CHINA) == host.toLowerCase(Locale.CHINA)
    }

    enum class NetworkType {
        SSO,
        NGX
    }

    @Synchronized
    fun getClient(type: NetworkType): OkHttpClient {
        if (!okhttpClientMap.containsKey(type)) {
            okhttpClientMap[type] = createClient(getCookieStore(type))
        }
        return okhttpClientMap[type]!!
    }

    @Synchronized
    fun getCookieStore(type: NetworkType): CookieStore {
        if (!cookieStoreMap.containsKey(type)) {
            cookieStoreMap[type] = createCookieStore(type)
        }
        return cookieStoreMap[type]!!
    }
}