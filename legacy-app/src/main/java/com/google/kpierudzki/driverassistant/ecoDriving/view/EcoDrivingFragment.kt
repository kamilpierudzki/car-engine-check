package com.google.kpierudzki.driverassistant.ecoDriving.view

import android.Manifest
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.*
import com.google.kpierudzki.driverassistant.*
import com.google.kpierudzki.driverassistant.common.view_components.TileFragment
import com.google.kpierudzki.driverassistant.common.view_components.aboutDialog.InfoDialog
import com.google.kpierudzki.driverassistant.ecoDriving.EcoDrivingContract
import com.google.kpierudzki.driverassistant.ecoDriving.EcoDrivingPresenter
import com.google.kpierudzki.driverassistant.geoSamples.connector.GpsProviderState
import com.google.kpierudzki.driverassistant.obd.datamodel.ObdParamType
import com.google.kpierudzki.driverassistant.util.UnitUtils
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.container_notification.*
import kotlinx.android.synthetic.main.fragment_eco_driving.*

/**
 * Created by Kamil on 25.06.2017.
 */

class EcoDrivingFragment
    : Fragment(),
        EcoDrivingContract.View,
        ActivityCompat.OnRequestPermissionsResultCallback,
        View.OnClickListener,
        IMenuCreate,
        TileFragment.Callbacks {

    private var ecoDrivingPresenter: EcoDrivingContract.Presenter? = null
    private var mainActivityFragmentsCallbacks: MainActivityFragmentsCallbacks? = null
    private var currentToolbarState = ToolbarState.UNINIT

    private lateinit var currentProvider: EcoDrivingContract.EcoDrivingDataProvider
    private lateinit var tileFragmentSpeed: TileFragment
    private lateinit var chartManager: AccelerationChartManager
    private lateinit var tileFragmentScore: TileFragment
    private lateinit var presenterTask: EcoDrivingPresenterTask
    private lateinit var chartDataSaver: EcoDrivingChartSaver

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_eco_driving, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        chartManager = AccelerationChartManager(App.getAppContext(), lineChart)
        chartDataSaver = EcoDrivingChartSaver()

        lineChart_Info.setOnClickListener { v ->
            (activity as? AppCompatActivity)?.also { compAct ->
                compAct.supportFragmentManager?.also { fm ->
                    val infoDialog = InfoDialog.newInstance(
                            getString(R.string.EcoDriving_About_Chart_Title),
                            getString(R.string.EcoDriving_About_Chart_Description))
                    infoDialog.show(fm, InfoDialog.TAG)
                }
            }
        }

        when (savedInstanceState) {
            null -> {
                (activity as? AppCompatActivity)?.also { compAct ->
                    tileFragmentSpeed = TileFragment.newInstance(ObdParamType.SPEED)
                    compAct.supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.frameLayout, tileFragmentSpeed, SPEED_CHART_TAG)
                            .commit()

                    tileFragmentScore = TileFragment.newInstance(ObdParamType.ECO_SCORE)
                    compAct.supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.frameLayout2, tileFragmentScore, SCORE_CHART_TAG)
                            .commit()
                }
            }

            else -> {
                (activity as? AppCompatActivity)?.also { compAct ->
                    tileFragmentSpeed = compAct.supportFragmentManager.findFragmentByTag(SPEED_CHART_TAG) as TileFragment
                    tileFragmentScore = compAct.supportFragmentManager.findFragmentByTag(SCORE_CHART_TAG) as TileFragment
                }

                chartManager.restoreValuesAndUpdateChart(
                        chartDataSaver.unroll(savedInstanceState))
            }
        }

        mainActivityFragmentsCallbacks?.onFragmentLoaded(
                MainActivityFragmentsCallbacks.LoadedFragment.EcoDriving)

        EcoDrivingPresenter(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        clearFindViewByIdCache()
        prepareAppBar(ToolbarState.UNINIT)
        ecoDrivingPresenter = null
    }

    override fun onStart() {
        super.onStart()
        ecoDrivingPresenter?.start()
    }

    override fun onStop() {
        super.onStop()
        ecoDrivingPresenter?.stop()
    }

    override fun setPresenter(presenter: EcoDrivingContract.Presenter) {
        this.ecoDrivingPresenter = presenter
        presenterTask = EcoDrivingPresenterTask(
                presenter,
                chartManager.chartEntriesLimit,
                tileFragmentScore.chartEntriesLimit,
                tileFragmentSpeed.chartEntriesLimit)
    }

    override fun onPresenterReady(presenter: EcoDrivingContract.Presenter) {
        presenterTask.invoke()
    }

    override fun updateSpeedClock(speed: Float) {
        if (tileFragmentSpeed.isVisible) {
            val speedInKmh = UnitUtils.mpsToKmh(speed)
            tileFragmentSpeed.updateChart(speedInKmh, null)
            tileFragmentSpeed.updateValue(speedInKmh, null)
        }
    }

    override fun updateScoreClock(score: Float) {
        if (tileFragmentScore.isVisible) {
            val correctedScore = score * 100
            tileFragmentScore.updateChart(correctedScore, null)
            tileFragmentScore.updateValue(correctedScore, null)
        }
    }

    override fun updateChart(currentAcceleration: Float) {
        chartManager.updateChart(currentAcceleration, null)
    }

    override fun updateGpsState(state: GpsProviderState) {
        when (state) {
            GpsProviderState.Disabled -> {
                notification_container.visibility = View.VISIBLE
                notification_container.setOnClickListener(null)
                notification_label.setText(R.string.GpsStatus_Label_NotEnabled)
            }
            GpsProviderState.Enabled -> notification_container.visibility = View.INVISIBLE
            GpsProviderState.NotSupported -> {
                notification_container.setOnClickListener(this)
                notification_container.visibility = View.VISIBLE
                notification_label.setText(R.string.GpsStatus_Label_NotSupported)

                (activity as? AppCompatActivity)?.also { compAct ->
                    ActivityCompat.requestPermissions(
                            compAct,
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            ACCESS_FINE_LOCATION_REQUEST)
                }
            }
        }
    }

    override fun onDataProviderChanged(provider: EcoDrivingContract.EcoDrivingDataProvider) {
        this.currentProvider = provider
        when (provider) {
            EcoDrivingContract.EcoDrivingDataProvider.Gps -> prepareAppBar(ToolbarState.GPS_PROVIDER)
            EcoDrivingContract.EcoDrivingDataProvider.Obd -> prepareAppBar(ToolbarState.OBD_PROVIDER)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        chartDataSaver.roll(chartManager.values, outState)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == ACCESS_FINE_LOCATION_REQUEST && grantResults[0] >= 0) {
            ecoDrivingPresenter?.also { p -> p.onPermissionGranted() }
        }
    }

    override fun onLastDataOfParam(data: List<Float>, parameter: EcoDrivingContract.EcoDrivingParameter) {
        when (parameter) {
            EcoDrivingContract.EcoDrivingParameter.ACCELERATION -> chartManager.restoreValuesAndUpdateChart(data)
            EcoDrivingContract.EcoDrivingParameter.SPEED -> tileFragmentSpeed.restoreValuesAndUpdateChart(data)
            EcoDrivingContract.EcoDrivingParameter.SCORE -> tileFragmentScore.restoreValuesAndUpdateChart(data)
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        try {
            this.mainActivityFragmentsCallbacks = activity as? MainActivityFragmentsCallbacks
        } catch (e: Exception) {
            Log.e("CalendarFragment", e.localizedMessage)
        }
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        try {
            this.mainActivityFragmentsCallbacks = getActivity() as? MainActivityFragmentsCallbacks
        } catch (e: Exception) {
            Log.e("CalendarFragment", e.localizedMessage)
        }
    }

    override fun onDetach() {
        super.onDetach()
        mainActivityFragmentsCallbacks = null
    }

    override fun onClick(view: View) {
        updateGpsState(GpsProviderState.NotSupported)
    }

    private fun prepareAppBar(toolbarState: ToolbarState) {
        (activity as? AppCompatActivity)?.also { compAct ->
            compAct.supportActionBar?.also { actionBar ->
                currentToolbarState = toolbarState
                when (toolbarState) {
                    ToolbarState.UNINIT -> {
                        actionBar.displayOptions = 0
                        (compAct as? MainActivity)?.menuCreateObservable = null
                    }

                    else -> {
                        actionBar.displayOptions = ActionBar.DISPLAY_SHOW_TITLE
                        actionBar.setTitle(R.string.Ecodriving_Name)
                        (compAct as? MainActivity)?.menuCreateObservable = this
                    }
                }
            }
            compAct.invalidateOptionsMenu()
        }
    }

    override fun onMenuCreated(menuInflater: MenuInflater, menu: Menu): Boolean {
        menuInflater.inflate(R.menu.eco_driving, menu)
        val providerItem = menu.findItem(R.id.eco_driving_provider)
        providerItem?.setIcon(currentToolbarState.iconRes)
        return true
    }

    override fun onOptionSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.eco_driving_provider -> {
                val infoDialog = when (currentProvider) {
                    EcoDrivingContract.EcoDrivingDataProvider.Gps -> InfoDialog.newInstance(
                            getString(R.string.EcoDriving_About_Provider_Title),
                            getString(R.string.EcoDriving_About_Provider_Description_Gps))
                    EcoDrivingContract.EcoDrivingDataProvider.Obd -> InfoDialog.newInstance(
                            getString(R.string.EcoDriving_About_Provider_Title),
                            getString(R.string.EcoDriving_About_Provider_Description_Bluetooth))
                }

                (activity as? AppCompatActivity)?.also { compAct ->
                    compAct.supportFragmentManager?.also { fm ->
                        infoDialog.show(fm, InfoDialog.TAG)
                    }
                }
            }
        }
        return true
    }

    override fun onNavigationBack(fromActivity: Boolean): Boolean {
        return false
    }

    override fun onAboutIconClicked(param: ObdParamType?) {
        val infoDialog = when (param) {
            ObdParamType.SPEED -> when (currentProvider) {
                EcoDrivingContract.EcoDrivingDataProvider.Gps -> InfoDialog.newInstance(
                        getString(R.string.EcoDriving_About_Speed_Title),
                        getString(R.string.EcoDriving_About_Speed_Description_GPS))
                EcoDrivingContract.EcoDrivingDataProvider.Obd -> InfoDialog.newInstance(
                        getString(R.string.EcoDriving_About_Speed_Title),
                        getString(R.string.EcoDriving_About_Speed_Description_Bluetooth))
            }

            ObdParamType.ECO_SCORE -> InfoDialog.newInstance(
                    getString(R.string.EcoDriving_About_Score_Title),
                    getString(R.string.EcoDriving_About_Score_Description))
            else -> null
        }

        if (infoDialog != null) {
            (activity as? AppCompatActivity)?.also { comAct ->
                comAct.supportFragmentManager?.also { fm ->
                    infoDialog.show(fm, InfoDialog.TAG)
                }
            }
        }
    }

    companion object {

        val ACCESS_FINE_LOCATION_REQUEST = 125
        val TAG = "EcoDrivingFragment_TAG"
        val SPEED_CHART_TAG = "SPEED_CHART_TAG"
        val SCORE_CHART_TAG = "SCORE_CHART_TAG"
        val ACCELERATION_CHART_DATA_KEY = "ACCELERATION_CHART_DATA_KEY"

        fun newInstance(): EcoDrivingFragment {
            return EcoDrivingFragment()
        }
    }
}

private enum class ToolbarState private constructor(val iconRes: Int) {
    UNINIT(0),
    GPS_PROVIDER(R.drawable.ic_gps),
    OBD_PROVIDER(R.drawable.ic_bluetooth_w)
}