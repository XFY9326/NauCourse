package tool.xfy9326.naucourse.providers.info.base

@Suppress("unused")
abstract class BaseMultiContentInfo<Type : Enum<*>, Param> : BaseContentInfo<Type, Param>() {
    suspend fun <E : Any> getInfo(type: Type, param: Param, loadCache: Boolean = false, forceRefresh: Boolean = false): InfoResult<E> =
        getInfo(type, setOf(param), loadCache, forceRefresh)

    suspend fun <E : Any> getInfo(
        type: Type,
        params: Set<Param> = emptySet(),
        loadCache: Boolean = false,
        forceRefresh: Boolean = false
    ): InfoResult<E> =
        getInfoProcess(type, params, loadCache, forceRefresh)
}