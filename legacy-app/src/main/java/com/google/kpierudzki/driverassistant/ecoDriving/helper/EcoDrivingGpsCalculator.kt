package com.google.kpierudzki.driverassistant.ecoDriving.helper

import android.support.annotation.WorkerThread
import com.google.kpierudzki.driverassistant.GlobalConfig
import com.google.kpierudzki.driverassistant.ecoDriving.database.EcoDrivingDao
import com.google.kpierudzki.driverassistant.ecoDriving.database.EcoDrivingEntity
import com.google.kpierudzki.driverassistant.geoSamples.connector.GeoSamplesSwappableData

/**
 * Created by Kamil on 08.10.2017.
 */

class EcoDrivingGpsCalculator(ecoDrivingDao: EcoDrivingDao, callbacks: EcoDrivingCalculatorCallbacks?, private val bufferLimit: Int)
    : BaseEcoDrivingCalculator(ecoDrivingDao, callbacks) {

    @WorkerThread
    override fun updateGeoData(geoData: GeoSamplesSwappableData) {
        synchronized(this) {
            val currentTime = System.currentTimeMillis()

            if (previousSpeed > -1 && previousTime > 0) {//first shoot
                //If user is not moving, stop calculations and don't insert data to buffer/database.
                if (isUserMoving(geoData)) {
                    val deltaSpeed = geoData.speed * 0.001f - previousSpeed * 0.001f//[m/ms]
                    val deltaTime = currentTime - previousTime//[ms]
                    val acceleration = deltaSpeed / deltaTime * 1.0f//[m/ms^2]
                    val currentScore = if (Math.abs(acceleration) <= GlobalConfig.ECO_DRIVING_GPS_OPTIMAL_ACCELERATION_LIMIT) 1 else 0
                    val avgScore = calculateAvgScore(true, geoData.trackId)

                    lastEcoDrivingEntity = EcoDrivingEntity(geoData, acceleration, currentScore, avgScore)

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
                    refreshDbStatistic(geoData.trackId)
                }
            }

            callbacks?.onSpeedChanged(geoData.speed)

            previousSpeed = geoData.speed
            previousTime = currentTime
        }
    }

    private fun isUserMoving(newData: GeoSamplesSwappableData): Boolean {
        return previousSpeed != 0f && newData.speed != 0f
    }
}
