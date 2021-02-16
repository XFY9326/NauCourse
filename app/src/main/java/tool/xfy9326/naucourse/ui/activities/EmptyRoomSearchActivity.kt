package tool.xfy9326.naucourse.ui.activities

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.DatePicker
import androidx.lifecycle.ViewModelProvider
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.constants.TimeConst
import tool.xfy9326.naucourse.databinding.ActivityEmptyRoomSearchBinding
import tool.xfy9326.naucourse.kt.bindLifecycle
import tool.xfy9326.naucourse.kt.enableHomeButton
import tool.xfy9326.naucourse.kt.showShortToast
import tool.xfy9326.naucourse.kt.showSnackBar
import tool.xfy9326.naucourse.providers.beans.jwc.EmptyRoomInfo
import tool.xfy9326.naucourse.providers.beans.jwc.EmptyRoomSearchParam
import tool.xfy9326.naucourse.ui.activities.base.ViewModelActivity
import tool.xfy9326.naucourse.ui.models.activity.EmptyRoomViewModel
import tool.xfy9326.naucourse.ui.views.recyclerview.adapters.EmptyRoomAdapter
import tool.xfy9326.naucourse.utils.views.DialogUtils
import tool.xfy9326.naucourse.utils.views.I18NUtils
import java.text.SimpleDateFormat
import java.util.*


class EmptyRoomSearchActivity : ViewModelActivity<EmptyRoomViewModel>(), DatePickerDialog.OnDateSetListener {
    private lateinit var adapter: EmptyRoomAdapter
    private var emptyRoomInfo: EmptyRoomInfo? = null

    companion object {
        private val DATE_FORMAT_YMD = SimpleDateFormat(TimeConst.FORMAT_YMD, Locale.CHINA)
    }

    private val binding by lazy {
        ActivityEmptyRoomSearchBinding.inflate(layoutInflater)
    }

    override fun onCreateContentView() = binding.root

    override fun onCreateViewModel(): EmptyRoomViewModel = ViewModelProvider(this)[EmptyRoomViewModel::class.java]

    override fun bindViewModel(viewModel: EmptyRoomViewModel) {
        viewModel.isLoading.observeEvent(this) {
            if (it) {
                binding.aslEmptyRoom.isRefreshing = true
            } else {
                binding.aslEmptyRoom.postStopRefreshing()
            }
        }
        viewModel.searchData.observe(this, {
            applySearchData(it)
        })
        viewModel.searchResult.observe(this, {
            adapter.submitList(it.toList())
        })
        viewModel.errorMsg.observeEvent(this) {
            if (it.second) {
                showShortToast(I18NUtils.getContentErrorResId(it.first)!!)
                finish()
            } else {
                binding.layoutEmptyRoom.showSnackBar(I18NUtils.getContentErrorResId(it.first)!!)
            }
        }
    }

    override fun initView(savedInstanceState: Bundle?, viewModel: EmptyRoomViewModel) {
        setSupportActionBar(binding.toolbar.tbGeneral)
        enableHomeButton()

        if (binding.etEmptyRoomDate.text.isNullOrEmpty() || binding.etEmptyRoomDate.text.isNullOrBlank()) {
            binding.etEmptyRoomDate.setText(DATE_FORMAT_YMD.format(Date()))
        }
        adapter = EmptyRoomAdapter(this)
        binding.list.arvDataList.enableEmptyViewShowDelay = false
        binding.list.arvDataList.adapter = adapter

        binding.aslEmptyRoom.setOnRefreshListener {
            viewModel.refreshSearchData()
        }

        binding.btnEmptyRoomDate.setOnClickListener {
            val calendar = Calendar.getInstance(Locale.CHINA).apply {
                time = DATE_FORMAT_YMD.parse(binding.etEmptyRoomDate.text.toString()) ?: Date()
            }
            DatePickerDialog(
                this,
                this,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).apply {
                bindLifecycle(lifecycle)
                show()
                DialogUtils.applyButtonTextAndBackground(this@EmptyRoomSearchActivity, this)
            }
        }
        binding.btnEmptyRoomSearch.setOnClickListener {
            if (binding.aslEmptyRoom.isRefreshing) {
                binding.layoutEmptyRoom.showSnackBar(R.string.empty_room_data_loading)
            } else {
                val data = emptyRoomInfo
                if (data != null) {
                    val startTime = binding.spEmptyRoomStart.selectedItem as EmptyRoomInfo.Time
                    val endTime = binding.spEmptyRoomEnd.selectedItem as EmptyRoomInfo.Time
                    if (startTime.num > endTime.num) {
                        binding.layoutEmptyRoom.showSnackBar(R.string.empty_room_course_num_error)
                        return@setOnClickListener
                    }
                    val searchDate = DATE_FORMAT_YMD.parse(binding.etEmptyRoomDate.text.toString()) ?: Date()
                    if (searchDate < data.startDate || searchDate > data.endDate) {
                        binding.layoutEmptyRoom.showSnackBar(
                            R.string.empty_room_date_error, DATE_FORMAT_YMD.format(data.startDate), DATE_FORMAT_YMD.format(data.endDate)
                        )
                        return@setOnClickListener
                    }

                    viewModel.searchData(
                        EmptyRoomSearchParam(
                            data.campusName.first().first, data.term, searchDate, data.startDate, data.endDate,
                            startTime.num, endTime.num
                        )
                    )
                } else {
                    binding.layoutEmptyRoom.showSnackBar(R.string.empty_room_data_loading)
                }
            }
        }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val calendar = Calendar.getInstance(Locale.CHINA).apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, dayOfMonth)
        }
        binding.etEmptyRoomDate.setText(DATE_FORMAT_YMD.format(calendar.time))
    }

    private fun applySearchData(data: EmptyRoomInfo) {
        this.emptyRoomInfo = data

        binding.tvEmptyRoomTerm.text = getString(R.string.empty_room_term, data.term)
        binding.tvEmptyRoomStartEndDate.text = getString(
            R.string.empty_room_start_end_date, DATE_FORMAT_YMD.format(data.startDate), DATE_FORMAT_YMD.format(data.endDate)
        )

        binding.spEmptyRoomStart.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, data.BJC).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        binding.spEmptyRoomEnd.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, data.EJC).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
    }
}