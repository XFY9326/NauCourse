package tool.xfy9326.naucourse.ui.fragments.base

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.ui.models.base.BaseViewModel
import kotlin.properties.Delegates

abstract class DrawerToolbarFragment<T : BaseViewModel> : ViewModelFragment<T>() {
    private var drawerIdRes by Delegates.notNull<Int>()

    companion object {
        const val DRAWER_ID = "DRAWER_ID"
    }

    override fun onSetInstance(args: Bundle?) {
        drawerIdRes = args?.getInt(DRAWER_ID)!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val toolbar = onBindToolbar()
        bindToolbarWithDrawer(toolbar)
        super.onViewCreated(view, savedInstanceState)
    }

    @Suppress("SameParameterValue")
    protected fun setToolbarTitleEnabled(enabled: Boolean) {
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(enabled)
    }

    private fun bindToolbarWithDrawer(toolbar: Toolbar) {
        (requireActivity() as AppCompatActivity).apply {
            val drawer = findViewById<DrawerLayout>(drawerIdRes)
            setSupportActionBar(toolbar)

            ActionBarDrawerToggle(requireActivity(), drawer, toolbar, R.string.drawer_layout_open, R.string.drawer_layout_close).apply {
                isDrawerIndicatorEnabled = true
                syncState()
                drawer.addDrawerListener(this)
            }
        }
    }

    protected abstract fun onBindToolbar(): Toolbar
}