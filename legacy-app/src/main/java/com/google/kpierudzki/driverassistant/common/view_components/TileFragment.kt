package com.google.kpierudzki.driverassistant.common.view_components

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.annotation.MainThread
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.kpierudzki.driverassistant.App
import com.google.kpierudzki.driverassistant.R
import com.google.kpierudzki.driverassistant.ecoDriving.view.EcoDrivingChartSaver
import com.google.kpierudzki.driverassistant.obd.datamodel.ObdParamType
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.tile_layout.*
import java.util.*

/**
 * Created by Kamil on 03.12.2017.
 */

class TileFragment : Fragment(), IChartAble {

    private val values = LinkedList<Float>()
    private var paramType: ObdParamType? = null
    private var callbacks: Callbacks? = null
    private val chartDataSaver = EcoDrivingChartSaver()
    private val tileLabelSaver = TileLabelSaver()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.tile_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.also { args ->
            paramType = (args.getSerializable(KEY_PARAM_TYPE) as? ObdParamType)?.also { p ->
                tile_title.setText(p.localizedName)
                tile_unit.setText(p.unit)
                tile_value.text = "--"
            }
        }

        styleChart()
        tile_about.setOnClickListener { callbacks?.onAboutIconClicked(paramType) }

        if (savedInstanceState != null) {
            restoreValuesAndUpdateChart(chartDataSaver.unroll(savedInstanceState))
            tile_value.text = tileLabelSaver.unroll(savedInstanceState)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        clearFindViewByIdCache()
    }

    private fun styleChart() {
        val description = Description()
        description.isEnabled = false
        tile_chart.description = description
        tile_chart.axisLeft.isEnabled = false
        tile_chart.axisRight.isEnabled = false
        tile_chart.xAxis.isEnabled = false
        tile_chart.setViewPortOffsets(0f, 0f, 0f, 0f)
        tile_chart.legend.isEnabled = false
        tile_chart.setScaleEnabled(false)
        tile_chart.setNoDataText("")
    }

    private fun styleLineDataSet(lineDataSet: LineDataSet) {
        lineDataSet.setDrawCircles(false)
        lineDataSet.setDrawCircleHole(false)
        lineDataSet.isHighlightEnabled = false
        lineDataSet.setDrawValues(false)
        lineDataSet.setDrawFilled(true)
        lineDataSet.fillColor = ContextCompat.getColor(App.getAppContext(), R.color.Tile_Chart_Background_Bottom)
        lineDataSet.setColors(ContextCompat.getColor(App.getAppContext(), R.color.Tile_Chart_Line))
    }

    override fun updateValue(newValue: Float, paramType: ObdParamType?) {
        tile_value.text = String.format(Locale.getDefault(), "%.0f", newValue)
    }

    @MainThread
    override fun updateChart(newValue: Float, paramType: ObdParamType?) {
        values.add(newValue)
        if (values.size > LIMIT) values.removeFirst()

        val entries = ArrayList<Entry>()
        for (i in values.indices) entries.add(Entry(i.toFloat(), values[i]))

        val lineDataSet = LineDataSet(entries, "")
        styleLineDataSet(lineDataSet)
        val lineData = LineData(lineDataSet)

        tile_chart.data = lineData
        tile_chart.invalidate()
    }

    @MainThread
    override fun restoreValuesAndUpdateChart(values: List<Float>) {
        if (values.isNotEmpty()) {
            for (i in 0 until values.size - 1) {
                //add all values except the last one
                this.values.add(values[i])
            }
            //add the last one now to trigger update of chart.
            updateChart(values[values.size - 1], null)
        }
    }

    override fun getValues(): List<Float> {
        return values
    }

    override fun getChartEntriesLimit(): Int {
        return LIMIT
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        chartDataSaver.roll(getValues(), outState)
        tileLabelSaver.roll(tile_value.text.toString(), outState)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        commonCallbackProvider()
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        commonCallbackProvider()
    }

    private fun commonCallbackProvider() {
        if (activity is AppCompatActivity) {
            val activity = activity as AppCompatActivity?
            if (activity != null) {
                val fragmentManager = activity.supportFragmentManager
                if (fragmentManager != null) {
                    for (fragment in fragmentManager.fragments) {
                        if (fragment is Callbacks) {
                            callbacks = fragment
                            return
                        }
                    }
                }
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    interface Callbacks {
        fun onAboutIconClicked(param: ObdParamType?)
    }

    companion object {

        val KEY_PARAM_TYPE = "KEY_PARAM_TYPE"
        private val LIMIT = 30
        private val CHART_DATA_KEY = "CHART_DATA_KEY"
        val CHART_VALUE_KEY = "CHART_VALUE_KEY"

        fun newInstance(paramType: ObdParamType): TileFragment {
            val newFragment = TileFragment()
            val args = Bundle()
            args.putSerializable(KEY_PARAM_TYPE, paramType)
            newFragment.arguments = args
            return newFragment
        }
    }
}

class TileLabelSaver : IUIStateSaver<String> {

    override fun roll(value: String, outState: Bundle) {
        outState.putString(TileFragment.CHART_VALUE_KEY, value)
    }

    override fun unroll(savedInstanceState: Bundle): String {
        return savedInstanceState.getString(TileFragment.CHART_VALUE_KEY, "")
    }
}