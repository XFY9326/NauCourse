package tool.xfy9326.naucourses.ui.activities

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.activity_user_info.*
import tool.xfy9326.naucourses.R
import tool.xfy9326.naucourses.ui.activities.base.ViewModelActivity
import tool.xfy9326.naucourses.ui.models.activity.UserInfoViewModel
import tool.xfy9326.naucourses.utils.views.ActivityUtils.enableHomeButton

class UserInfoActivity : ViewModelActivity<UserInfoViewModel>() {
    override fun onCreateContentView(): Int = R.layout.activity_user_info

    override fun onCreateViewModel(): UserInfoViewModel = ViewModelProvider(this)[UserInfoViewModel::class.java]

    override fun initView(savedInstanceState: Bundle?, viewModel: UserInfoViewModel) {
        setSupportActionBar(tb_userInfo)
        enableHomeButton()
    }
}