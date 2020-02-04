package tool.xfy9326.naucourses.io.configs

import tool.xfy9326.naucourses.io.configs.base.BaseConfig
import tool.xfy9326.naucourses.io.configs.utils.RuntimeAdapter

object RuntimeConfig : BaseConfig<RuntimeAdapter>() {
    override fun getStoreAdapter(): RuntimeAdapter = RuntimeAdapter
}