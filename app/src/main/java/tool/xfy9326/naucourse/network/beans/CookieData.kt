package tool.xfy9326.naucourse.network.beans

abstract class CookieData {
    abstract val host: String
    abstract val name: String
    abstract val value: String
    abstract val expiresAt: Long
    abstract val domain: String
    abstract val path: String
    abstract val secure: Boolean
    abstract val httpOnly: Boolean
    abstract val hostOnly: Boolean
    abstract val persistent: Boolean
}