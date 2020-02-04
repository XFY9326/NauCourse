package tool.xfy9326.naucourses.io.configs.base

class BaseKey<T : Any>(val group: BaseGroup, val itemName: String, val storeAdapter: BaseAdapter, val defaultValue: T) {
    companion object {
        const val PARAM_JOIN_SYMBOL = "_"
    }

    val keyValue: String = "${group.getValue()}$PARAM_JOIN_SYMBOL$itemName"

    override fun hashCode(): Int {
        return keyValue.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other.hashCode() == hashCode()
    }
}