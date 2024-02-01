package com.google.kpierudzki.driverassistant.ecoDriving.helper

import android.support.annotation.WorkerThread

import com.google.kpierudzki.driverassistant.GlobalConfig
import com.google.kpierudzki.driverassistant.ecoDriving.database.EcoDrivingDao
import com.google.kpierudzki.driverassistant.ecoDriving.database.EcoDrivingEntity
import com.google.kpierudzki.driverassistant.geoSamples.connector.GeoSamplesSwappableData
import com.google.kpierudzki.driverassistant.geoSamples.connector.IGeoSampleListener

/**
 * Created by Kamil on 06.10.2017.
 */

class EcoDrivingObdCalculator : BaseEcoDrivingCalculator {

    private var currentGeoData: GeoSamplesSwappableData
    private val bufferLimit: Int

    constructor(ecoDrivingDao: EcoDrivingDao, callbacks: EcoDrivingCalculatorCallbacks?, bufferLimit: Int)
            : super(ecoDrivingDao, callbacks) {
        this.bufferLimit = bufferLimit
        currentGeoData = GeoSamplesSwappableData(0, 0, 0f)
    }

    @WorkerThread
    override fun updateGeoData(geoData: GeoSamplesSwappableData) {
        synchronized(this) {
            this.currentGeoData = GeoSamplesSwappableData(geoData)
        }
    }

    @WorkerThread
    fun onNewSpeed(newSpeed: Float) {
        synchronized(this) {
            val currentTime = System.currentTimeMillis()

            if (previousSpeed > -1 && previousTime > 0) {//first shoot
                //If user is not moving, stop calculations and don't insert data to buffer/database.
                if (isUserMoving(newSpeed)) {
                    val deltaSpeed = newSpeed * 0.001f - previousSpeed * 0.001f//[m/ms]
                    val deltaTime = currentTime - previousTime//[ms]
                    val acceleration = deltaSpeed / deltaTime * 1.0f//[m/ms^2]
                    val currentScore = if (Math.abs(acceleration) <= GlobalConfig.ECO_DRIVING_OBD_OPTIMAL_ACCELERATION_LIMIT) 1 else 0
                    val avgScore = calculateAvgScore(true, currentGeoData.trackId)

                    lastEcoDrivingEntity = EcoDrivingEntity(currentGeoData, acceleration, currentScore, avgScore)

                    callbacks?.let {
                        it.onAccelerationChanged(acceleration)
                        it.onAvgScoreChanged(avgScore)
                    }
                }
            }


            lastEcoDrivingEntity?.let {
                buffer.add(it)

                if (buffer.size >= bufferLimit) {
                    ecoDrivingDao.addAll(buffer)
                    buffer.clear()
                    refreshDbStatistic(currentGeoData.trackId)
                }
            }

            callbacks?.onSpeedChanged(newSpeed)

            previousSpeed = newSpeed
            previousTime = currentTime
        }
    }

    private fun isUserMoving(newSpeed: Float): Boolean {
        return previousSpeed != 0f && newSpeed != 0f
    }
}
