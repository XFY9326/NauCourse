package tool.xfy9326.naucourse.ui.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.activity_user_info.*
import kotlinx.android.synthetic.main.view_card_credit_info.*
import kotlinx.android.synthetic.main.view_card_learning_process.*
import kotlinx.android.synthetic.main.view_card_rank_info.*
import kotlinx.android.synthetic.main.view_card_user_info.*
import kotlinx.android.synthetic.main.view_grid_text_item.view.*
import kotlinx.android.synthetic.main.view_learning_process_item.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tool.xfy9326.naucourse.Constants
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.providers.beans.jwc.StudentInfo
import tool.xfy9326.naucourse.providers.beans.jwc.StudentLearningProcess
import tool.xfy9326.naucourse.providers.beans.jwc.StudentPersonalInfo
import tool.xfy9326.naucourse.providers.beans.jwc.StudentPersonalInfo.Companion.toPlainText
import tool.xfy9326.naucourse.providers.contents.methods.jwc.StudentIndex
import tool.xfy9326.naucourse.ui.activities.base.ViewModelActivity
import tool.xfy9326.naucourse.ui.models.activity.UserInfoViewModel
import tool.xfy9326.naucourse.utils.utility.IntentUtils
import tool.xfy9326.naucourse.utils.views.ActivityUtils.enableHomeButton
import tool.xfy9326.naucourse.utils.views.I18NUtils

class UserInfoActivity : ViewModelActivity<UserInfoViewModel>() {
    private lateinit var inflater: LayoutInflater

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inflater = LayoutInflater.from(this)
    }

    override fun onCreateContentView(): Int = R.layout.activity_user_info

    override fun onCreateViewModel(): UserInfoViewModel = ViewModelProvider(this)[UserInfoViewModel::class.java]

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_user_info, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_personalPhoto) {
            IntentUtils.viewUrlPhoto(this, StudentIndex.JWC_STU_PHOTO_URL.toString())
        }
        return super.onOptionsItemSelected(item)
    }

    override fun initView(savedInstanceState: Bundle?, viewModel: UserInfoViewModel) {
        setSupportActionBar(tb_userInfo)
        enableHomeButton()
    }

    override fun bindViewModel(viewModel: UserInfoViewModel) {
        viewModel.studentInfo.observe(this, Observer {
            updateView(it)
        })
    }

    @Synchronized
    private fun updateView(studentInfo: StudentInfo) {
        lifecycleScope.launch(Dispatchers.Main) {
            launch { updateBaseInfo(studentInfo.personalInfo) }
            launch { updateLearningProcess(studentInfo.learningProcess) }
            launch { updateCreditInfo(studentInfo.creditInfo) }
            launch { updateRankInfo(studentInfo.rankingInfo) }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateBaseInfo(studentPersonalInfo: StudentPersonalInfo) {
        tv_userInfoId.text = studentPersonalInfo.stuId.toPlainText()
        tv_userInfoName.text = studentPersonalInfo.name.toPlainText()
        tv_userInfoGrade.text = studentPersonalInfo.grade.toPlainText()
        tv_userInfoCollege.text = studentPersonalInfo.college.toPlainText()
        tv_userInfoMajor.text = studentPersonalInfo.major.toPlainText()
        tv_userInfoMajorDirection.text = studentPersonalInfo.majorDirection.toPlainText()
        tv_userInfoTrainingDirection.text = studentPersonalInfo.trainingDirection.toPlainText()
        tv_userInfoCurrentClass.text = studentPersonalInfo.currentClass.toPlainText()
    }

    @SuppressLint("SetTextI18n")
    private fun updateLearningProcess(learningProcess: Array<StudentLearningProcess>) {
        layout_userLearningProcess.removeViewsInLayout(1, layout_userLearningProcess.childCount - 1)
        for (process in learningProcess) {
            layout_userLearningProcess.addViewInLayout(
                inflater.inflate(R.layout.view_learning_process_item, layout_userLearningProcess, false).apply {
                    tv_processCourseType.setText(I18NUtils.getCourseTypeResId(process.courseType))
                    pb_processBar.progress = process.progress
                    tv_processPercent.text = process.progress.toString() + Constants.PERCENT
                    val views = arrayOfNulls<View>(process.subjects.size)
                    var i = 0
                    for (entry in process.subjects) {
                        views[i++] = inflater.inflate(R.layout.view_grid_text_item, gl_processCourseType, false).apply {
                            tv_gridItemText.text = getString(I18NUtils.getLearningProcessSubjectTypeResId(entry.key), entry.value)
                        }
                    }
                    gl_processCourseType.replaceAllViews(views.requireNoNulls(), false)
                })
        }
        layout_userLearningProcess.refreshLayout()
    }

    @SuppressLint("SetTextI18n")
    private fun updateCreditInfo(creditInfo: LinkedHashMap<String, Float>) {
        val views = arrayOfNulls<View>(creditInfo.size)
        var i = 0
        for (entry in creditInfo) {
            views[i++] = inflater.inflate(R.layout.view_grid_text_item, gl_userCreditInfo, false).apply {
                tv_gridItemText.text = "${entry.key}：${entry.value}"
            }
        }
        gl_userCreditInfo.replaceAllViews(views.requireNoNulls())
    }

    @SuppressLint("SetTextI18n")
    private fun updateRankInfo(rankingInfo: LinkedHashMap<String, String>) {
        val views = arrayOfNulls<View>(rankingInfo.size)
        var i = 0
        for (entry in rankingInfo) {
            views[i++] = inflater.inflate(R.layout.view_grid_text_item, gl_userRankInfo, false).apply {
                tv_gridItemText.text = entry.key + entry.value
            }
        }
        gl_userRankInfo.replaceAllViews(views.requireNoNulls())
    }

    override fun onDestroy() {
        System.gc()
        super.onDestroy()
    }
}