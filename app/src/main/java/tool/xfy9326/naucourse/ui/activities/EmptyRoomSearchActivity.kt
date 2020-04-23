package tool.xfy9326.naucourse.ui.activities

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.DatePicker
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.activity_empty_room_search.*
import kotlinx.android.synthetic.main.layout_list.*
import kotlinx.android.synthetic.main.view_general_toolbar.*
import tool.xfy9326.naucourse.Constants
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.providers.beans.jwc.EmptyRoomInfo
import tool.xfy9326.naucourse.providers.beans.jwc.EmptyRoomSearchParam
import tool.xfy9326.naucourse.ui.activities.base.ViewModelActivity
import tool.xfy9326.naucourse.ui.models.activity.EmptyRoomViewModel
import tool.xfy9326.naucourse.ui.views.recyclerview.adapters.EmptyRoomAdapter
import tool.xfy9326.naucourse.utils.views.ActivityUtils.enableHomeButton
import tool.xfy9326.naucourse.utils.views.ActivityUtils.showSnackBar
import tool.xfy9326.naucourse.utils.views.ActivityUtils.showToast
import tool.xfy9326.naucourse.utils.views.DialogUtils
import tool.xfy9326.naucourse.utils.views.I18NUtils
import java.text.SimpleDateFormat
import java.util.*


class EmptyRoomSearchActivity : ViewModelActivity<EmptyRoomViewModel>(), DatePickerDialog.OnDateSetListener {
    private lateinit var adapter: EmptyRoomAdapter
    private var emptyRoomInfo: EmptyRoomInfo? = null

    companion object {
        private val DATE_FORMAT_YMD = SimpleDateFormat(Constants.Time.FORMAT_YMD, Locale.CHINA)
    }

    override fun onCreateContentView(): Int = R.layout.activity_empty_room_search

    override fun onCreateViewModel(): EmptyRoomViewModel = ViewModelProvider(this)[EmptyRoomViewModel::class.java]

    override fun bindViewModel(viewModel: EmptyRoomViewModel) {
        viewModel.isLoading.observeEvent(this, Observer {
            asl_emptyRoom.post {
                asl_emptyRoom.isRefreshing = it
            }
        })
        viewModel.searchData.observe(this, Observer {
            applySearchData(it)
        })
        viewModel.searchResult.observe(this, Observer {
            adapter.submitList(it.toList())
        })
        viewModel.errorMsg.observeEvent(this, Observer {
            if (it.second) {
                showToast(I18NUtils.getContentErrorResId(it.first)!!)
                finish()
            } else {
                showSnackBar(layout_emptyRoom, I18NUtils.getContentErrorResId(it.first)!!)
            }
        })
    }

    override fun initView(savedInstanceState: Bundle?, viewModel: EmptyRoomViewModel) {
        setSupportActionBar(tb_general)
        enableHomeButton()

        if (et_emptyRoomDate.text.isNullOrEmpty() || et_emptyRoomDate.text.isNullOrBlank()) {
            et_emptyRoomDate.setText(DATE_FORMAT_YMD.format(Date()))
        }
        adapter = EmptyRoomAdapter(this)
        arv_dataList.enableEmptyViewShowDelay = false
        arv_dataList.adapter = adapter

        asl_emptyRoom.setOnRefreshListener {
            viewModel.refreshSearchData()
        }

        btn_emptyRoomDate.setOnClickListener {
            val calendar = Calendar.getInstance(Locale.CHINA).apply {
                time = DATE_FORMAT_YMD.parse(et_emptyRoomDate.text.toString()) ?: Date()
            }
            DatePickerDialog(
                this,
                this,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).apply {
                DialogUtils.addAutoCloseListener(lifecycle, this)
                show()
                DialogUtils.applyButtonTextAndBackground(this@EmptyRoomSearchActivity, this)
            }
        }
        btn_emptyRoomSearch.setOnClickListener {
            if (asl_emptyRoom.isRefreshing) {
                showSnackBar(layout_emptyRoom, R.string.empty_room_data_loading)
            } else {
                val data = emptyRoomInfo
                if (data != null) {
                    val startTime = sp_emptyRoomStart.selectedItem as EmptyRoomInfo.Time
                    val endTime = sp_emptyRoomEnd.selectedItem as EmptyRoomInfo.Time
                    if (startTime.num > endTime.num) {
                        showSnackBar(layout_emptyRoom, R.string.empty_room_course_num_error)
                        return@setOnClickListener
                    }
                    val searchDate = DATE_FORMAT_YMD.parse(et_emptyRoomDate.text.toString()) ?: Date()
                    if (searchDate < data.startDate || searchDate > data.endDate) {
                        showSnackBar(
                            this, layout_emptyRoom,
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
                    showSnackBar(layout_emptyRoom, R.string.empty_room_data_loading)
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
        et_emptyRoomDate.setText(DATE_FORMAT_YMD.format(calendar.time))
    }

    private fun applySearchData(data: EmptyRoomInfo) {
        this.emptyRoomInfo = data

        tv_emptyRoomTerm.text = getString(R.string.empty_room_term, data.term)
        tv_emptyRoomStartEndDate.text = getString(
            R.string.empty_room_start_end_date, DATE_FORMAT_YMD.format(data.startDate), DATE_FORMAT_YMD.format(data.endDate)
        )

        sp_emptyRoomStart.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, data.BJC).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        sp_emptyRoomEnd.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, data.EJC).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
    }
}