package tool.xfy9326.naucourse.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.lifecycle.ViewModelProvider
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.beans.SerializableNews
import tool.xfy9326.naucourse.databinding.FragmentNewsBinding
import tool.xfy9326.naucourse.io.prefs.SettingsPref
import tool.xfy9326.naucourse.kt.showSnackBar
import tool.xfy9326.naucourse.providers.beans.GeneralNews
import tool.xfy9326.naucourse.ui.activities.NewsDetailActivity
import tool.xfy9326.naucourse.ui.dialogs.NewsTypeChoiceDialog
import tool.xfy9326.naucourse.ui.fragments.base.DrawerToolbarFragment
import tool.xfy9326.naucourse.ui.models.fragment.NewsViewModel
import tool.xfy9326.naucourse.ui.views.recyclerview.adapters.NewsAdapter
import tool.xfy9326.naucourse.utils.utility.IntentUtils
import tool.xfy9326.naucourse.utils.views.I18NUtils

class NewsFragment : DrawerToolbarFragment<NewsViewModel>(), NewsAdapter.OnNewsItemClickListener, NewsTypeChoiceDialog.OnNewsTypeChangedListener {
    private lateinit var newsAdapter: NewsAdapter
    private var _binding: FragmentNewsBinding? = null
    private val binding
        get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = view
        return if (v == null) {
            val binding = FragmentNewsBinding.inflate(layoutInflater, container, false).also {
                this._binding = it
            }
            binding.root
        } else {
            val parent = requireView().parent as ViewGroup?
            parent?.removeView(v)
            _binding = FragmentNewsBinding.bind(v)
            v
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateViewModel(): NewsViewModel = ViewModelProvider(this)[NewsViewModel::class.java]

    override fun onBindToolbar() = binding.toolbar.tbGeneral.apply {
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
        viewModel.isRefreshing.observe(viewLifecycleOwner, {
            binding.aslNews.postStopRefreshing()
        })
        viewModel.newsList.observe(viewLifecycleOwner, {
            newsAdapter.submitList(it)
            binding.arvNewsList.scrollToPosition(0)
        })
        viewModel.errorMsg.observeEvent(viewLifecycleOwner) {
            binding.layoutNews.showSnackBar(I18NUtils.getContentErrorResId(it)!!)
        }
    }

    override fun onStart() {
        if (SettingsPref.AutoAsyncNewsInfo) {
            getViewModel().refreshNewsList()
        }
        super.onStart()
    }

    override fun initView(viewModel: NewsViewModel) {
        newsAdapter = NewsAdapter(requireContext(), this)

        binding.toolbar.tbGeneral.setOnClickListener {
            binding.arvNewsList.smoothScrollToPosition(0)
        }

        binding.arvNewsList.adapter = newsAdapter
        binding.aslNews.setOnRefreshListener {
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