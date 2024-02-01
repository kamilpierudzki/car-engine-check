package com.google.kpierudzki.driverassistant.ecoDriving.view

import android.content.Context
import android.support.annotation.MainThread
import android.support.v4.content.ContextCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.kpierudzki.driverassistant.GlobalConfig
import com.google.kpierudzki.driverassistant.R
import com.google.kpierudzki.driverassistant.common.view_components.IChartAble
import com.google.kpierudzki.driverassistant.obd.datamodel.ObdParamType
import java.util.*

/**
 * Created by Kamil on 05.09.2017.
 */

class AccelerationChartManager(private val context: Context, private val chartWidget: LineChart) : IChartAble {

    private val values = LinkedList<Float>()

    private val CHART_ENTRIES_LIMIT: Int = context.resources.getInteger(R.integer.EcoDriving_Chart_Entries)

    init {
        styleChart()
    }

    override fun updateValue(newValue: Float, paramType: ObdParamType) {
        //Ignore
    }

    @MainThread
    override fun updateChart(newValue: Float, paramType: ObdParamType?) {
        values.addLast(newValue)
        if (values.size > CHART_ENTRIES_LIMIT)
            values.removeFirst()

        val accelerationEntries = ArrayList<Entry>()
        for (i in values.indices)
            accelerationEntries.add(Entry(i.toFloat(), values[i]))

        val scoreDataSet = LineDataSet(accelerationEntries, context.getString(R.string.EcoDriving_Chart_DataSet))
        styleLineDataSet(scoreDataSet)

        chartWidget.data = LineData(scoreDataSet)
        chartWidget.invalidate()
    }

    @MainThread
    override fun restoreValuesAndUpdateChart(values: List<Float>) {
        if (values.isNotEmpty()) {
            for (i in 0 until values.size - 1) {//add all values except the last one
                this.values.add(values[i])
            }
            updateChart(values[values.size - 1], null)//add the last one now to trigger update of chart.
        }
    }

    override fun getValues(): List<Float> {
        return values
    }

    override fun getChartEntriesLimit(): Int {
        return CHART_ENTRIES_LIMIT
    }

    private fun styleChart() {
        chartWidget.xAxis.isEnabled = false

        val leftYAxis = chartWidget.axisLeft
        leftYAxis.textColor = ContextCompat.getColor(context, R.color.EcoDriving_Chart_Line)

        val maxLimit = LimitLine(
                GlobalConfig.ECO_DRIVING_GPS_OPTIMAL_ACCELERATION_LIMIT.toFloat(),
                context.getString(R.string.EcoDriving_Chart_Optimal))
        maxLimit.lineColor = ContextCompat.getColor(context, R.color.EcoDriving_Chart_OptimalLimits)
        maxLimit.lineWidth = 2f
        leftYAxis.addLimitLine(maxLimit)

        val minLimit = LimitLine(
                (GlobalConfig.ECO_DRIVING_GPS_OPTIMAL_ACCELERATION_LIMIT * -1f).toFloat(),
                context.getString(R.string.EcoDriving_Chart_Optimal))
        minLimit.lineColor = ContextCompat.getColor(context, R.color.EcoDriving_Chart_OptimalLimits)
        minLimit.lineWidth = 2f
        leftYAxis.addLimitLine(minLimit)

        leftYAxis.axisMaximum = maxLimit.limit * 1.5f
        leftYAxis.axisMinimum = minLimit.limit * 1.5f
        leftYAxis.setDrawGridLines(false)

        chartWidget.setTouchEnabled(false)
        chartWidget.axisRight.isEnabled = false
        chartWidget.setViewPortOffsets(0f, 0f, 0f, 0f)
        chartWidget.setNoDataText(context.resources.getString(R.string.EcoDriving_Chart_NoData))

        val description = Description()
        description.isEnabled = false
        chartWidget.description = description
    }

    private fun styleLineDataSet(lineDataSet: LineDataSet) {
        lineDataSet.color = ContextCompat.getColor(context, R.color.EcoDriving_Chart_Line)
        lineDataSet.setDrawCircles(false)
        lineDataSet.setDrawValues(false)
        lineDataSet.lineWidth = 1.5f
    }
}
