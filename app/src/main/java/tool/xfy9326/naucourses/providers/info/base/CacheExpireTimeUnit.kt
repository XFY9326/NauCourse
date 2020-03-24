package tool.xfy9326.naucourses.providers.info.base

@Suppress("unused")
enum class CacheExpireTimeUnit {
    MINUTE {
        override val offset: Long = 60 * 1000
    },
    HOUR {
        override val offset: Long = 60 * 60 * 1000
    },
    DAY {
        override val offset: Long = 24 * 60 * 60 * 1000
    };

    protected abstract val offset: Long
    fun toMillis(int: Int): Long = int * offset
    fun getTimeInTimeUnit(long: Long): Long = (long / offset) * offset
}