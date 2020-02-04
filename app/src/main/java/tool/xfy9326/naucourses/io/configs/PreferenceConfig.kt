package tool.xfy9326.naucourses.io.configs

import android.content.Context
import tool.xfy9326.naucourses.io.configs.base.BaseConfig
import tool.xfy9326.naucourses.io.configs.utils.PreferenceAdapter

class PreferenceConfig private constructor(private val context: Context) : BaseConfig<PreferenceAdapter>() {
    companion object {
        @Volatile
        private var adapter: PreferenceAdapter? = null

        @Volatile
        private lateinit var instance: PreferenceConfig

        private const val CONFIG_PREFERENCE = "ConfigPref"

        private fun getAdapter(context: Context) = adapter ?: synchronized(this) {
            adapter ?: PreferenceAdapter(context, CONFIG_PREFERENCE).also { adapter = it }
        }

        fun initInstance(context: Context) = synchronized(this) {
            if (!::instance.isInitialized) {
                instance = PreferenceConfig(context)
            }
        }

        fun getInstance(): PreferenceConfig = instance
    }

    override fun getStoreAdapter(): PreferenceAdapter = getAdapter(context)

}