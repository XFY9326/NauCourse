package tool.xfy9326.naucourse.ui.activities

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_score_query.*
import kotlinx.android.synthetic.main.view_general_toolbar.*
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.ui.activities.base.ViewModelActivity
import tool.xfy9326.naucourse.ui.models.fragment.ScoreQueryViewModel
import tool.xfy9326.naucourse.ui.views.viewpager.ScoreQueryViewPagerAdapter
import tool.xfy9326.naucourse.utils.views.ActivityUtils
import tool.xfy9326.naucourse.utils.views.ActivityUtils.enableHomeButton
import tool.xfy9326.naucourse.utils.views.I18NUtils

class ScoreQueryActivity : ViewModelActivity<ScoreQueryViewModel>() {
    override fun onCreateContentView(): Int = R.layout.activity_score_query

    override fun onCreateViewModel(): ScoreQueryViewModel = ViewModelProvider(this)[ScoreQueryViewModel::class.java]

    override fun bindViewModel(viewModel: ScoreQueryViewModel) {
        viewModel.errorMsg.observeEvent(this, Observer {
            ActivityUtils.showSnackBar(layout_scoreQuery, I18NUtils.getContentErrorResId(it)!!)
        })
        viewModel.isRefreshing.observe(this, Observer {
            asl_scoreQuery.post {
                asl_scoreQuery.isRefreshing = it
            }
        })
    }

    override fun initView(savedInstanceState: Bundle?, viewModel: ScoreQueryViewModel) {
        setSupportActionBar(tb_general)
        enableHomeButton()

        vp_scoreQuery.apply {
            offscreenPageLimit = 1
            adapter = ScoreQueryViewPagerAdapter(this@ScoreQueryActivity)
        }

        tb_general.setOnClickListener {
            viewModel.scrollToTop.notifyEvent()
        }

        asl_scoreQuery.setOnRefreshListener {
            viewModel.refreshData()
        }

        TabLayoutMediator(tabLayout_scoreQuery, vp_scoreQuery,
            TabLayoutMediator.TabConfigurationStrategy { tab, position ->
                when (position) {
                    0 -> tab.setText(R.string.current_term_score)
                    1 -> tab.setText(R.string.history_course_score)
                }
            }).attach()
    }
}