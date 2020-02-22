package tool.xfy9326.naucourses.ui.models.activity

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import tool.xfy9326.naucourses.providers.beans.jwc.StudentInfo
import tool.xfy9326.naucourses.providers.info.methods.CardBalanceInfo
import tool.xfy9326.naucourses.providers.info.methods.PersonalInfo
import tool.xfy9326.naucourses.ui.activities.MainDrawerActivity
import tool.xfy9326.naucourses.ui.models.base.BaseViewModel

class MainDrawerViewModel : BaseViewModel() {

    companion object {
        private val DEFAULT_FRAGMENT = MainDrawerActivity.Companion.FragmentType.COURSE_TABLE
    }

    var nowShowFragmentType = DEFAULT_FRAGMENT
    val studentCardBalance = MutableLiveData<Float>()
    val studentInfo = MutableLiveData<StudentInfo>()

    override fun onActivityCreate() {
        updatePersonalInfo(true)
        updateBalance(true)
    }

    override fun onInitView() {
        updatePersonalInfo()
    }

    private fun updatePersonalInfo(initLoad: Boolean = false) {
        viewModelScope.launch {
            val personalInfo = PersonalInfo.getInfo(loadCache = initLoad)
            if (personalInfo.isSuccess) {
                studentInfo.postValue(personalInfo.data!!)
            } else {
                Log.d(this@MainDrawerViewModel.javaClass.simpleName, "PersonalInfo Error: ${personalInfo.errorReason}")
            }
        }
    }

    fun updateBalance(initLoad: Boolean = false) {
        viewModelScope.launch {
            val balance = CardBalanceInfo.getInfo(loadCache = initLoad)
            if (balance.isSuccess) {
                studentCardBalance.postValue(balance.data!!)
            } else {
                Log.d(this@MainDrawerViewModel.javaClass.simpleName, "CardBalanceInfo Error: ${balance.errorReason}")
            }
        }
    }
}