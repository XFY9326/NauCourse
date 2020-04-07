package tool.xfy9326.naucourse.providers.store

import tool.xfy9326.naucourse.providers.beans.ykt.CardBalance
import tool.xfy9326.naucourse.providers.store.base.BaseJsonStore

object CardBalanceStore : BaseJsonStore<CardBalance>() {
    override val fileName: String = "CardBalance"
    override val versionCode: Int = 1
    override val storeClass: Class<CardBalance> = CardBalance::class.java

    override val useCache: Boolean = false
    override val useEncrypt: Boolean = true
}