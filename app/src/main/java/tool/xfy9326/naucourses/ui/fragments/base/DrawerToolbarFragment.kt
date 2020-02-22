package tool.xfy9326.naucourses.ui.fragments.base

import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import tool.xfy9326.naucourses.R
import tool.xfy9326.naucourses.ui.models.base.BaseViewModel
import kotlin.properties.Delegates

abstract class DrawerToolbarFragment<T : BaseViewModel> : ViewModelFragment<T>() {
    private var drawerIdRes by Delegates.notNull<Int>()

    companion object {
        const val DRAWER_ID = "DRAWER_ID"
    }

    override fun onSetInstance(args: Bundle?) {
        drawerIdRes = args?.getInt(DRAWER_ID)!!
    }

    override fun onStart() {
        super.onStart()
        val toolbar = onBindToolbar()
        bindToolbarWithDrawer(toolbar)
    }

    private fun bindToolbarWithDrawer(toolbar: Toolbar) {
        val drawer = activity!!.findViewById<DrawerLayout>(drawerIdRes)
        ActionBarDrawerToggle(activity!!, drawer, toolbar, R.string.drawer_layout_open, R.string.drawer_layout_close).apply {
            isDrawerIndicatorEnabled = true
            syncState()
            drawer.addDrawerListener(this)
        }
    }

    protected abstract fun onBindToolbar(): Toolbar
}