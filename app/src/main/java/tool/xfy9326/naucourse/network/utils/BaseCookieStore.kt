package tool.xfy9326.naucourse.network.utils

import okhttp3.CookieJar

interface BaseCookieStore : CookieJar {
    fun clearCookies()
}