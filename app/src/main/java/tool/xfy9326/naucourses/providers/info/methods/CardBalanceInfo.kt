package tool.xfy9326.naucourses.providers.info.methods

import tool.xfy9326.naucourses.io.prefs.UserPref
import tool.xfy9326.naucourses.providers.contents.base.ContentResult
import tool.xfy9326.naucourses.providers.contents.methods.my.StudentCardBalance
import tool.xfy9326.naucourses.providers.info.base.BaseSimpleContentInfo

object CardBalanceInfo : BaseSimpleContentInfo<Float, Nothing>() {
    override fun loadSimpleStoredInfo(): Float? {
        val balance = UserPref.CardBalance
        return if (balance == UserPref.CARD_BALANCE_DEFAULT_VALUE) {
            null
        } else {
            balance
        }
    }

    override suspend fun getSimpleInfoContent(params: Set<Nothing>): ContentResult<Float> = StudentCardBalance.getContentData()

    override fun saveSimpleInfo(info: Float) {
        UserPref.CardBalance = info
    }

    override fun clearSimpleStoredInfo() = UserPref.remove(UserPref.CARD_BALANCE)
}