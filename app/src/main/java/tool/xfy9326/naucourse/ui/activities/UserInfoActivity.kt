package tool.xfy9326.naucourse.ui.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.ViewModelProvider
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.constants.BaseConst
import tool.xfy9326.naucourse.databinding.ActivityUserInfoBinding
import tool.xfy9326.naucourse.databinding.ViewGridTextItemBinding
import tool.xfy9326.naucourse.databinding.ViewLearningProcessItemBinding
import tool.xfy9326.naucourse.kt.enableHomeButton
import tool.xfy9326.naucourse.kt.showSnackBar
import tool.xfy9326.naucourse.network.LoginNetworkManager
import tool.xfy9326.naucourse.providers.beans.jwc.StudentInfo
import tool.xfy9326.naucourse.providers.beans.jwc.StudentLearningProcess
import tool.xfy9326.naucourse.providers.beans.jwc.StudentPersonalInfo
import tool.xfy9326.naucourse.providers.beans.jwc.StudentPersonalInfo.Companion.toPlainText
import tool.xfy9326.naucourse.ui.activities.base.ViewModelActivity
import tool.xfy9326.naucourse.ui.models.activity.UserInfoViewModel
import tool.xfy9326.naucourse.utils.views.I18NUtils

class UserInfoActivity : ViewModelActivity<UserInfoViewModel>() {
    private val binding by lazy {
        ActivityUserInfoBinding.inflate(layoutInflater)
    }

    override fun onCreateContentView() = binding.root

    override fun onCreateViewModel(): UserInfoViewModel = ViewModelProvider(this)[UserInfoViewModel::class.java]

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_user_info, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_personalPhoto) {
            getViewModel().requestStuPhoto()
        } else if (item.itemId == R.id.menu_refreshUserInfo) {
            getViewModel().updatePersonalInfo(forceRefresh = true)
            binding.layoutActivityUserInfo.showSnackBar(R.string.refreshing_user_info)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun initView(savedInstanceState: Bundle?, viewModel: UserInfoViewModel) {
        setSupportActionBar(binding.tbUserInfo)
        enableHomeButton()
    }

    override fun bindViewModel(viewModel: UserInfoViewModel) {
        viewModel.studentInfo.observe(this, {
            updateView(it)
        })
        viewModel.studentPhotoUrl.observeEvent(this) {
            ImageShowActivity.showImageActivity(this, it, LoginNetworkManager.ClientType.JWC)
        }
        viewModel.userInfoRefreshFailed.observeEvent(this) {
            binding.layoutActivityUserInfo.showSnackBar(I18NUtils.getContentErrorResId(it)!!)
        }
    }

    @Synchronized
    private fun updateView(studentInfo: StudentInfo) {
        updateBaseInfo(studentInfo.personalInfo)
        updateLearningProcess(studentInfo.learningProcess)
        updateCreditInfo(studentInfo.creditInfo)
        updateRankInfo(studentInfo.rankingInfo)
    }

    @SuppressLint("SetTextI18n")
    private fun updateBaseInfo(studentPersonalInfo: StudentPersonalInfo) {
        binding.content.userInfo.apply {
            tvUserInfoId.text = studentPersonalInfo.stuId.toPlainText()
            tvUserInfoName.text = studentPersonalInfo.name.toPlainText()
            tvUserInfoGrade.text = studentPersonalInfo.grade.toPlainText()
            tvUserInfoCollege.text = studentPersonalInfo.college.toPlainText()
            tvUserInfoMajor.text = studentPersonalInfo.major.toPlainText()
            tvUserInfoMajorDirection.text = studentPersonalInfo.majorDirection.toPlainText()
            tvUserInfoTrainingDirection.text = studentPersonalInfo.trainingDirection.toPlainText()
            tvUserInfoCurrentClass.text = studentPersonalInfo.currentClass.toPlainText()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateLearningProcess(learningProcess: Array<StudentLearningProcess>) {
        binding.content.learningProcess.apply {
            layoutUserLearningProcess.removeViewsInLayout(1, layoutUserLearningProcess.childCount - 1)
            for (process in learningProcess) {
                layoutUserLearningProcess.addView(
                    ViewLearningProcessItemBinding.inflate(layoutInflater, layoutUserLearningProcess, false).apply {
                        tvProcessCourseType.setText(I18NUtils.getCourseTypeResId(process.courseType))
                        pbProcessBar.progress = process.progress
                        tvProcessPercent.text = process.progress.toString() + BaseConst.PERCENT
                        val views = arrayOfNulls<View>(process.subjects.size)
                        var i = 0
                        for (entry in process.subjects) {
                            views[i++] = ViewGridTextItemBinding.inflate(layoutInflater, glProcessCourseType, false).apply {
                                tvGridItemText.text = getString(I18NUtils.getLearningProcessSubjectTypeResId(entry.key), entry.value)
                            }.root
                        }
                        glProcessCourseType.replaceAllViews(views.requireNoNulls(), false)
                    }.root
                )
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateCreditInfo(creditInfo: LinkedHashMap<String, Float>) {
        val views = arrayOfNulls<View>(creditInfo.size)
        var i = 0
        for (entry in creditInfo) {
            views[i++] = ViewGridTextItemBinding.inflate(layoutInflater, binding.content.creditInfo.glUserCreditInfo, false).apply {
                tvGridItemText.text = "${entry.key}：${entry.value}"
            }.root
        }
        binding.content.creditInfo.glUserCreditInfo.replaceAllViews(views.requireNoNulls())
    }

    @SuppressLint("SetTextI18n")
    private fun updateRankInfo(rankingInfo: LinkedHashMap<String, String>) {
        val views = arrayOfNulls<View>(rankingInfo.size)
        var i = 0
        for (entry in rankingInfo) {
            views[i++] = ViewGridTextItemBinding.inflate(layoutInflater, binding.content.rankInfo.glUserRankInfo, false).apply {
                tvGridItemText.text = entry.key + entry.value
            }.root
        }
        binding.content.rankInfo.glUserRankInfo.replaceAllViews(views.requireNoNulls())
    }
}