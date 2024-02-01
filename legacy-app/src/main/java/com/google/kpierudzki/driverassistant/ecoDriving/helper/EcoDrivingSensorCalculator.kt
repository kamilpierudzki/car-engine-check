package com.google.kpierudzki.driverassistant.ecoDriving.helper

import android.support.annotation.WorkerThread
import com.google.kpierudzki.driverassistant.ecoDriving.database.EcoDrivingDao
import com.google.kpierudzki.driverassistant.geoSamples.connector.GeoSamplesSwappableData

class EcoDrivingSensorCalculator : BaseEcoDrivingCalculator {

    private var currentGeoData: GeoSamplesSwappableData
    private val bufferLimit: Int

    constructor(ecoDrivingDao: EcoDrivingDao, callbacks: EcoDrivingCalculatorCallbacks?, bufferLimit: Int)
            : super(ecoDrivingDao, callbacks) {
        this.bufferLimit = bufferLimit
        currentGeoData = GeoSamplesSwappableData(0, 0, 0f)
    }

    override fun updateGeoData(geoData: GeoSamplesSwappableData) {
        synchronized(this) {
            //todo
        }
    }

    @WorkerThread
    fun onNewAcceleration() {
        synchronized(this) {
            //todo
        }
    }
}