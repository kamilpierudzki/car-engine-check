package com.google.kpierudzki.driverassistant.common.service

import android.location.Location
import android.support.annotation.MainThread
import com.google.kpierudzki.driverassistant.common.connector.IPermissionRequirable
import com.google.kpierudzki.driverassistant.common.connector.IPersistableBuffer
import com.google.kpierudzki.driverassistant.geoSamples.connector.GeoSamplesSwappableData
import com.google.kpierudzki.driverassistant.geoSamples.connector.GpsProviderState
import com.google.kpierudzki.driverassistant.geoSamples.connector.IGeoSampleListener
import com.google.kpierudzki.driverassistant.geoSamples.connector.IGeoSampleObservable
import com.google.kpierudzki.driverassistant.service.connector.ManagerConnectorType
import com.google.kpierudzki.driverassistant.service.manager.BaseServiceManager
import com.google.kpierudzki.driverassistant.util.MainThreadUtil

abstract class GeoDataBasedManager : BaseServiceManager, IGeoSampleListener, IPersistableBuffer,
        IPermissionRequirable {

    protected var geoSampleObservable: IGeoSampleObservable?

    constructor(geoSampleObservable: IGeoSampleObservable?, types: Array<ManagerConnectorType>) : super(types) {
        this.geoSampleObservable = geoSampleObservable
    }

    @MainThread
    override fun onDestroy() {
        if (threadPool != null && !threadPool.isTerminating) {
            threadPool.execute {
                synchronized(this) {
                    geoSampleObservable = null
                    persistBuffers()
                    MainThreadUtil.post { super.onDestroy() }
                }
            }
        } else {
            super.onDestroy()
        }
    }

    override fun forcePersistBuffer(async: Boolean) {
        if (async) {
            if (threadPool != null && !threadPool.isTerminating) {
                threadPool.execute { this.persistBuffers() }
            }
        } else {
            persistBuffers()
        }
    }

    open fun persistBuffers() {
        synchronized(this) {
            geoSampleObservable?.forcePersistBuffer(false)
        }
    }

    override fun onPermissionGranted() {
        geoSampleObservable?.onPermissionGranted()
    }

    override fun onRawLocation(location: Location) {
        // Ignore
    }

    override fun onNewData(newData: GeoSamplesSwappableData) {
        // Ignore
    }

    override fun onGpsProviderStateChanged(state: GpsProviderState) {
        // Ignore
    }
}