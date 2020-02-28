package tool.xfy9326.naucourses.ui.fragments

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
import tool.xfy9326.naucourses.R
import tool.xfy9326.naucourses.beans.SerializableNews
import tool.xfy9326.naucourses.io.prefs.AppPref
import tool.xfy9326.naucourses.providers.beans.GeneralNews
import tool.xfy9326.naucourses.ui.activities.NewsDetailActivity
import tool.xfy9326.naucourses.ui.dialogs.NewsTypeChoiceDialog
import tool.xfy9326.naucourses.ui.fragments.base.DrawerToolbarFragment
import tool.xfy9326.naucourses.ui.models.fragment.NewsViewModel
import tool.xfy9326.naucourses.ui.views.recyclerview.adapters.NewsAdapter
import tool.xfy9326.naucourses.utils.IntentUtils
import tool.xfy9326.naucourses.utils.views.ActivityUtils.showSnackBar
import tool.xfy9326.naucourses.utils.views.I18NUtils

class NewsFragment : DrawerToolbarFragment<NewsViewModel>(), NewsAdapter.OnNewsItemClickListener {
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
            R.id.menu_newsShowType -> NewsTypeChoiceDialog().apply {
                setTypeChangedListener(object : NewsTypeChoiceDialog.OnNewsTypeChangedListener {
                    override fun onChanged() {
                        getViewModel().refreshNewsList()
                    }
                })
            }.show(childFragmentManager, null)
        }
        return super.onOptionsItemSelected(item)
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
        viewModel.errorMsg.observeSingle(viewLifecycleOwner, Observer {
            showSnackBar(layout_news, I18NUtils.getContentErrorResId(it)!!)
        })
    }

    override fun initView(viewModel: NewsViewModel) {
        newsAdapter = NewsAdapter(requireContext(), this)

        arv_newsList.adapter = newsAdapter
        asl_newsRefreshLayout.setOnRefreshListener {
            viewModel.refreshNewsList()
        }
    }

    override fun onNewsItemClick(news: GeneralNews) {
        if (AppPref.DefaultShowNewsInBrowser) {
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