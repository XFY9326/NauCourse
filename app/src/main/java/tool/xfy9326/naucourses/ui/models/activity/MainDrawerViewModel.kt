package tool.xfy9326.naucourses.ui.models.activity

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tool.xfy9326.naucourses.providers.beans.jwc.StudentInfo
import tool.xfy9326.naucourses.providers.info.methods.CardBalanceInfo
import tool.xfy9326.naucourses.providers.info.methods.PersonalInfo
import tool.xfy9326.naucourses.ui.activities.MainDrawerActivity
import tool.xfy9326.naucourses.ui.models.base.BaseViewModel

class MainDrawerViewModel : BaseViewModel() {
    private val logTag = javaClass.simpleName

    private var hasInitFragmentShow = false
    var nowShowFragmentType = MainDrawerActivity.Companion.FragmentType.NEWS
    val studentCardBalance = MutableLiveData<Float>()
    val studentInfo = MutableLiveData<StudentInfo>()

    override fun onInitView(isRestored: Boolean) {
        updatePersonalInfo(true)
        updateBalance(true)
        updatePersonalInfo()
    }

    @Synchronized
    fun initFragmentShow() =
        if (hasInitFragmentShow) {
            true
        } else {
            hasInitFragmentShow = true
            false
        }

    private fun updatePersonalInfo(initLoad: Boolean = false) {
        viewModelScope.launch(Dispatchers.Default) {
            val personalInfo = PersonalInfo.getInfo(loadCache = initLoad)
            if (personalInfo.isSuccess) {
                studentInfo.postValue(personalInfo.data!!)
            } else {
                Log.d(logTag, "PersonalInfo Error: ${personalInfo.errorReason}")
            }
        }
    }

    fun updateBalance(initLoad: Boolean = false) {
        viewModelScope.launch(Dispatchers.Default) {
            val balance = CardBalanceInfo.getInfo(loadCache = initLoad)
            if (balance.isSuccess) {
                studentCardBalance.postValue(balance.data!!)
            } else {
                Log.d(logTag, "CardBalanceInfo Error: ${balance.errorReason}")
            }
        }
    }
}