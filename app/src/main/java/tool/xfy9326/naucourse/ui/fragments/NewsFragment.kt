package tool.xfy9326.naucourse.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.fragment_news.*
import kotlinx.android.synthetic.main.view_general_toolbar.*
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.beans.SerializableNews
import tool.xfy9326.naucourse.io.prefs.SettingsPref
import tool.xfy9326.naucourse.providers.beans.GeneralNews
import tool.xfy9326.naucourse.ui.activities.NewsDetailActivity
import tool.xfy9326.naucourse.ui.dialogs.NewsTypeChoiceDialog
import tool.xfy9326.naucourse.ui.fragments.base.DrawerToolbarFragment
import tool.xfy9326.naucourse.ui.models.fragment.NewsViewModel
import tool.xfy9326.naucourse.ui.views.recyclerview.adapters.NewsAdapter
import tool.xfy9326.naucourse.utils.utility.IntentUtils
import tool.xfy9326.naucourse.utils.views.ActivityUtils.showSnackBar
import tool.xfy9326.naucourse.utils.views.I18NUtils

class NewsFragment : DrawerToolbarFragment<NewsViewModel>(), NewsAdapter.OnNewsItemClickListener, NewsTypeChoiceDialog.OnNewsTypeChangedListener {
    private lateinit var newsAdapter: NewsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        setHasOptionsMenu(true)
    }

    override fun onCreateContentView(): Int = R.layout.fragment_news

    override fun onCreateViewModel(): NewsViewModel = ViewModelProvider(this)[NewsViewModel::class.java]

    override fun onBindToolbar(): Toolbar = tb_general.apply {
        title = getString(R.string.news)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.menu_news, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_newsShowType -> NewsTypeChoiceDialog().show(childFragmentManager, null)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNewsTypeChanged() {
        getViewModel().refreshNewsList()
    }

    override fun bindViewModel(viewModel: NewsViewModel) {
        viewModel.isRefreshing.observe(viewLifecycleOwner, Observer {
            asl_newsRefreshLayout.post {
                asl_newsRefreshLayout.isRefreshing = it
            }
        })
        viewModel.newsList.observe(viewLifecycleOwner, Observer {
            newsAdapter.updateNewsList(it)
        })
        viewModel.errorMsg.observeEvent(viewLifecycleOwner, Observer {
            showSnackBar(layout_news, I18NUtils.getContentErrorResId(it)!!)
        })
    }

    override fun initView(viewModel: NewsViewModel) {
        newsAdapter = NewsAdapter(requireContext(), this)

        tb_general.setOnClickListener {
            arv_newsList.smoothScrollToPosition(0)
        }

        arv_newsList.adapter = newsAdapter
        asl_newsRefreshLayout.setOnRefreshListener {
            viewModel.refreshNewsList()
        }
    }

    override fun onNewsItemClick(news: GeneralNews) {
        if (SettingsPref.UseBrowserOpenNewsDetail) {
            IntentUtils.launchUrlInBrowser(requireContext(), news.detailUrl.toString())
        } else {
            startActivity(
                Intent(requireContext(), NewsDetailActivity::class.java).putExtra(
                    NewsDetailActivity.NEWS_DATA,
                    SerializableNews.parse(news)
                )
            )
        }
    }
}