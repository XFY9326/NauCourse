package tool.xfy9326.naucourse.ui.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_score_query.*
import kotlinx.android.synthetic.main.view_general_toolbar.*
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.beans.CreditCountItem
import tool.xfy9326.naucourse.kt.enableHomeButton
import tool.xfy9326.naucourse.kt.showSnackBar
import tool.xfy9326.naucourse.ui.activities.base.ViewModelActivity
import tool.xfy9326.naucourse.ui.dialogs.CreditCountCourseSelectDialog
import tool.xfy9326.naucourse.ui.models.activity.ScoreQueryViewModel
import tool.xfy9326.naucourse.ui.views.viewpager.ScoreQueryViewPagerAdapter
import tool.xfy9326.naucourse.utils.views.DialogUtils
import tool.xfy9326.naucourse.utils.views.I18NUtils

class ScoreQueryActivity : ViewModelActivity<ScoreQueryViewModel>(), CreditCountCourseSelectDialog.OnCreditCountCourseSelectedListener {
    override fun onCreateContentView(): Int = R.layout.activity_score_query

    override fun onCreateViewModel(): ScoreQueryViewModel = ViewModelProvider(this)[ScoreQueryViewModel::class.java]

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_score_query, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_scoreQueryCreditCount) {
            getViewModel().requestCreditCount()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun bindViewModel(viewModel: ScoreQueryViewModel) {
        viewModel.errorMsg.observeEvent(this, {
            layout_scoreQuery.showSnackBar(I18NUtils.getContentErrorResId(it)!!)
        })
        viewModel.isRefreshing.observe(this, {
            if (it) {
                asl_scoreQuery.isRefreshing = true
            } else {
                asl_scoreQuery.postStopRefreshing()
            }
        })
        viewModel.credit.observeEvent(this, {
            DialogUtils.createCreditShowDialog(this@ScoreQueryActivity, lifecycle, it).show()
        })
        viewModel.creditCountStatus.observeEvent(this, {
            layout_scoreQuery.showSnackBar(I18NUtils.getCreditCountStatusResId(it))
        })
        viewModel.creditCourseSelect.observeEvent(this, {
            CreditCountCourseSelectDialog().apply {
                arguments = Bundle().apply {
                    putSerializable(CreditCountCourseSelectDialog.CREDIT_COUNT_SELECT_ITEM, it.first)
                    putSerializable(CreditCountCourseSelectDialog.CREDIT_COUNT_HISTORY_ITEM, it.second)
                }
            }.show(supportFragmentManager, null)
        })
    }

    override fun initView(savedInstanceState: Bundle?, viewModel: ScoreQueryViewModel) {
        setSupportActionBar(tb_general)
        enableHomeButton()

        vp_scoreQuery.apply {
            offscreenPageLimit = 2
            adapter = ScoreQueryViewPagerAdapter(this@ScoreQueryActivity)
        }

        tb_general.setOnClickListener {
            viewModel.scrollToTop.notifyEvent()
        }

        asl_scoreQuery.setOnRefreshListener {
            viewModel.refreshData(forceUpdate = true)
        }

        TabLayoutMediator(
            tabLayout_scoreQuery, vp_scoreQuery
        ) { tab, position ->
            when (position) {
                0 -> tab.setText(R.string.current_term_score)
                1 -> tab.setText(R.string.history_course_score)
            }
        }.attach()
    }

    override fun onCreditCountCourseSelected(items: ArrayList<CreditCountItem>, history: ArrayList<CreditCountItem>) {
        getViewModel().requestCreditCount(items, history)
    }
}