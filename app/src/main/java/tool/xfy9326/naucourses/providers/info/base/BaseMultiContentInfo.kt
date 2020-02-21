package tool.xfy9326.naucourses.providers.info.base

abstract class BaseMultiContentInfo<T : Enum<*>, P : Enum<*>> : BaseContentInfo<T, P>() {
    suspend fun <E : Any> getInfo(type: T, param: P, loadCache: Boolean = false, forceRefresh: Boolean = false): InfoResult<E> =
        getInfo(type, setOf(param), loadCache, forceRefresh)

    suspend fun <E : Any> getInfo(type: T, params: Set<P> = emptySet(), loadCache: Boolean = false, forceRefresh: Boolean = false): InfoResult<E> =
        getInfoProcess(type, params, loadCache, forceRefresh)
}