package com.google.kpierudzki.driverassistant.ecoDriving.helper

import android.support.annotation.WorkerThread
import com.google.kpierudzki.driverassistant.ecoDriving.database.EcoDrivingDao
import com.google.kpierudzki.driverassistant.ecoDriving.database.EcoDrivingEntity
import com.google.kpierudzki.driverassistant.ecoDriving.database.EcoDrivingStatistic
import com.google.kpierudzki.driverassistant.geoSamples.connector.GeoSamplesSwappableData
import com.google.kpierudzki.driverassistant.geoSamples.connector.IGeoSampleListener

/**
 * Created by Kamil on 08.10.2017.
 */

abstract class BaseEcoDrivingCalculator(var ecoDrivingDao: EcoDrivingDao, protected var callbacks: EcoDrivingCalculatorCallbacks?) {
    var previousSpeed: Float = -1f
    var buffer = arrayListOf<EcoDrivingEntity>()
    private var currentDbStatistic: EcoDrivingStatistic? = null
    var previousTime: Long = -1
    var lastEcoDrivingEntity: EcoDrivingEntity? = null

    @WorkerThread
    abstract fun updateGeoData(geoData: GeoSamplesSwappableData)

    fun calculateAvgScore(includePersistedData: Boolean, trackId: Long): Float {
        if (currentDbStatistic == null) {
            refreshDbStatistic(trackId)
        }

        val sumOfScore = buffer.map { it.currentScore }.fold(0) { acc, i -> acc + i }

        if (buffer.isEmpty()) {
            return 1f
        }

        return if (includePersistedData) {
            (currentDbStatistic!!.sum + sumOfScore) / (currentDbStatistic!!.count + buffer.size * 1.0f)
        } else {
            sumOfScore / (buffer.size * 1.0f)
        }
    }

    fun refreshDbStatistic(trackId: Long) {
        currentDbStatistic = EcoDrivingStatistic.merge(
                ecoDrivingDao.getScoreStatisticsForTrackId(trackId),
                ecoDrivingDao.getCountStatisticsForTrackId(trackId))
    }

    @WorkerThread
    fun forcePersistBuffer() {
        synchronized(this) {
            if (!buffer.isEmpty()) {
                ecoDrivingDao.addAll(buffer)
                buffer.clear()
                currentDbStatistic = null
            }
        }
    }
}
