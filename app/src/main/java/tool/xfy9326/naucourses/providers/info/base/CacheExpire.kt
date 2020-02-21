package tool.xfy9326.naucourses.providers.info.base

data class CacheExpire(
    val expireRule: CacheExpireRule = CacheExpireRule.INSTANTLY,
    val expireTime: Int = 0,
    val expireTimeUnit: CacheExpireTimeUnit = CacheExpireTimeUnit.MINUTE
)