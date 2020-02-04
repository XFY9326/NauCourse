package tool.xfy9326.naucourses.io.configs.base

class BaseGroup(private val groupName: String) {
    fun getValue(): String = groupName

    override fun hashCode(): Int {
        return getValue().hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other.hashCode() == hashCode()
    }
}