package com.google.kpierudzki.driverassistant.geoSamples.connector


import android.location.Location

import com.google.kpierudzki.driverassistant.service.connector.IBaseManager

/**
 * Created by Kamil on 16.07.2017.
 */

interface IGeoSampleListener : IBaseManager.IBaseManagerListener {
    fun onNewData(newData: GeoSamplesSwappableData)

    fun onGpsProviderStateChanged(state: GpsProviderState)

    fun onRawLocation(location: Location)
}

class GeoSamplesSwappableData {
    var trackId: Long = 0
    var offset: Long = 0
    var speed: Float = 0.toFloat()

    constructor(trackId: Long, offset: Long, speed: Float) {
        this.trackId = trackId
        this.offset = offset
        this.speed = speed
    }

    constructor(data: GeoSamplesSwappableData) {
        this.trackId = data.trackId
        this.offset = data.offset
        this.speed = data.speed
    }
}

enum class GpsProviderState {
    Enabled,
    Disabled,
    NotSupported
}