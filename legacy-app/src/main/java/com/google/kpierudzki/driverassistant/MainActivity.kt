package com.google.kpierudzki.driverassistant

import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.multidex.BuildConfig
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.view.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.kpierudzki.driverassistant.common.IDestroyable
import com.google.kpierudzki.driverassistant.debug.export_main_db.DebugDrawerExportDb
import com.google.kpierudzki.driverassistant.debug.gps_recording.view.DebugDrawerGpsProbesRecordingManager
import com.google.kpierudzki.driverassistant.debug.obd_recording.view.DebugDrawerObdProbesRecordingManager
import com.google.kpierudzki.driverassistant.dtc.view.DtcFragment
import com.google.kpierudzki.driverassistant.ecoDriving.view.EcoDrivingFragment
import com.google.kpierudzki.driverassistant.history.calendar.view.HistoryCalendarFragment
import com.google.kpierudzki.driverassistant.menu.about.MenuDrawerAbout
import com.google.kpierudzki.driverassistant.menu.demoModeSwitcher.MenuDemoModeSwitcher
import com.google.kpierudzki.driverassistant.obd.start.view.ObdStartFragment
import com.kamil.pierudzki.legacy_app.R
import java.util.*

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener, DrawerLayout.DrawerListener, MainActivityFragmentsCallbacks {

    private val menuControls = ArrayList<IDestroyable>()
    private var navigationSelectedId = -1
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    var menuCreateObservable: IMenuCreate? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        main_activity_version.text = BuildConfig.VERSION_NAME
        if (GlobalConfig.DEBUG_MODE) {
            nav_debug_layout.addView(LayoutInflater.from(this).inflate(R.layout.nav_debug_layout, null, false))
        }

        prepareAppBar()

        drawer_layout.addDrawerListener(this)
        actionBarDrawerToggle = ActionBarDrawerToggle(this, drawer_layout, toolbar, 0, 0)
        actionBarDrawerToggle.syncState()
        actionBarDrawerToggle.setToolbarNavigationClickListener { view -> supportFragmentManager.popBackStack() }

        bottomNavigationView.setOnNavigationItemSelectedListener(this)

        if (savedInstanceState == null) {
            bottomNavigationView.selectedItemId = R.id.navigation_ecodriving

            val intent = intent
            if (intent != null && intent.hasExtra(DEMO_MODE_KEY)) {
                GlobalConfig.DEMO_MODE = intent.getBooleanExtra(DEMO_MODE_KEY, false)
            }
        }

        val view = findViewById<View>(android.R.id.content)
        if (GlobalConfig.DEBUG_MODE) {
            menuControls.add(DebugDrawerGpsProbesRecordingManager(view, fragmentManager, drawer_layout, main_frame))
            menuControls.add(DebugDrawerObdProbesRecordingManager(view, fragmentManager, drawer_layout, main_frame))
            menuControls.add(DebugDrawerExportDb(view))
        }
        menuControls.add(MenuDemoModeSwitcher(view))
        menuControls.add(MenuDrawerAbout(view, supportFragmentManager, drawer_layout))
    }

    override fun onDestroy() {
        super.onDestroy()
        clearFindViewByIdCache()
        menuControls.forEach { it.onDestroy() }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        if (navigationSelectedId != R.id.navigation_ecodriving && item.itemId == R.id.navigation_ecodriving) {
            removeAllFragments()
            supportFragmentManager.beginTransaction()
                    .replace(main_frame.id, EcoDrivingFragment.newInstance(), EcoDrivingFragment.TAG)
                    .commit()
            navigationSelectedId = R.id.navigation_ecodriving
            return true
        } else if (navigationSelectedId != R.id.navigation_history && item.itemId == R.id.navigation_history) {
            removeAllFragments()
            supportFragmentManager.beginTransaction()
                    .replace(main_frame.id, HistoryCalendarFragment.newInstance(), HistoryCalendarFragment.TAG)
                    .commit()
            navigationSelectedId = R.id.navigation_history
            return true
        } else if (navigationSelectedId != R.id.navigation_obd2 && item.itemId == R.id.navigation_obd2) {
            removeAllFragments()
            supportFragmentManager.beginTransaction()
                    .replace(main_frame.id, ObdStartFragment.newInstance(), ObdStartFragment.TAG)
                    .commit()
            navigationSelectedId = R.id.navigation_obd2
            return true
        } else if (navigationSelectedId != R.id.navigation_dtc && item.itemId == R.id.navigation_dtc) {
            removeAllFragments()
            supportFragmentManager.beginTransaction()
                    .replace(main_frame.id, DtcFragment.newInstance(), DtcFragment.TAG)
                    .commit()
            navigationSelectedId = R.id.navigation_dtc
            return true
        }
        return false
    }

    private fun removeAllFragments() {
        for (i in 0 until supportFragmentManager.backStackEntryCount) {
            supportFragmentManager.popBackStack()
        }

        val fragmentTransaction = supportFragmentManager.beginTransaction()
        supportFragmentManager.fragments.forEach { fragmentTransaction.remove(it) }
        fragmentTransaction.commit()
    }

    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
        //...
    }

    override fun onDrawerOpened(drawerView: View) {
        //...
    }

    override fun onDrawerClosed(drawerView: View) {
        //...
    }

    override fun onDrawerStateChanged(newState: Int) {
        //...
    }

    @TargetApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == EcoDrivingFragment.ACCESS_FINE_LOCATION_REQUEST && grantResults.size > 0 && grantResults[0] >= 0) {
            val ecoDrivingFragment = supportFragmentManager.findFragmentByTag(EcoDrivingFragment.TAG)
            ecoDrivingFragment?.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onFragmentLoaded(loadedFragment: MainActivityFragmentsCallbacks.LoadedFragment) {
        when (loadedFragment) {
            MainActivityFragmentsCallbacks.LoadedFragment.EcoDriving -> {
                bottomNavigationView.menu.getItem(0).isChecked = true
                navigationSelectedId = R.id.navigation_ecodriving
            }
            MainActivityFragmentsCallbacks.LoadedFragment.ObdII -> {
                bottomNavigationView.menu.getItem(1).isChecked = true
                navigationSelectedId = R.id.navigation_obd2
            }
            MainActivityFragmentsCallbacks.LoadedFragment.History -> {
                bottomNavigationView.menu.getItem(2).isChecked = true
                navigationSelectedId = R.id.navigation_history
            }
            MainActivityFragmentsCallbacks.LoadedFragment.Dtc -> {
                bottomNavigationView.menu.getItem(3).isChecked = true
                navigationSelectedId = R.id.navigation_dtc
            }
        }
    }

    override fun getRootContainer(): ViewGroup {
        return main_frame
    }

    override fun setUseToolbarNavigationCustomAction(enabled: Boolean) {
        actionBarDrawerToggle.isDrawerIndicatorEnabled = !enabled
        actionBarDrawerToggle.syncState()

        if (enabled) {
            toolbar!!.setNavigationOnClickListener { v ->
                if (menuCreateObservable != null)
                    menuCreateObservable!!.onNavigationBack(false)
            }
        } else {
            toolbar!!.setNavigationOnClickListener { v -> drawer_layout.openDrawer(Gravity.START, true) }
        }
    }

    private fun prepareAppBar() {
        setSupportActionBar(toolbar)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return if (menuCreateObservable != null) {
            menuCreateObservable!!.onMenuCreated(menuInflater, menu)
        } else {
            super.onCreateOptionsMenu(menu)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (menuCreateObservable != null) {
            menuCreateObservable!!.onOptionSelected(item)
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if (menuCreateObservable == null || !menuCreateObservable!!.onNavigationBack(true)) {
            super.onBackPressed()
        }
    }

    companion object {

        val DEMO_MODE_KEY = "demo_mode_key"

        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }
}
