package com.google.kpierudzki.driverassistant.ecoDriving.view

import android.os.Bundle
import com.google.kpierudzki.driverassistant.common.view_components.IUIStateSaver

class EcoDrivingChartSaver : IUIStateSaver<List<Float>> {

    override fun unroll(savedInstanceState: Bundle): List<Float> {
        val restoredValues = savedInstanceState.getFloatArray(EcoDrivingFragment.ACCELERATION_CHART_DATA_KEY)
        return restoredValues?.toList() ?: emptyList()
    }

    override fun roll(value: List<Float>, outState: Bundle) {
        val toStore = FloatArray(value.size)
        for (i in 0 until value.size) toStore[i] = value[i]
        outState.putFloatArray(EcoDrivingFragment.ACCELERATION_CHART_DATA_KEY, toStore)
    }
}