package com.google.kpierudzki.driverassistant.ecoDriving.service

import android.location.Location
import android.support.annotation.WorkerThread
import com.google.kpierudzki.driverassistant.App
import com.google.kpierudzki.driverassistant.ecoDriving.connector.IEcoDrivingObservable
import com.google.kpierudzki.driverassistant.ecoDriving.helper.EcoDrivingCalculatorCallbacks
import com.google.kpierudzki.driverassistant.ecoDriving.helper.EcoDrivingSensorCalculator
import com.google.kpierudzki.driverassistant.geoSamples.connector.GeoSamplesSwappableData
import com.google.kpierudzki.driverassistant.geoSamples.connector.GpsProviderState
import com.google.kpierudzki.driverassistant.geoSamples.connector.IGeoSampleListener
import com.google.kpierudzki.driverassistant.geoSamples.connector.IGeoSampleObservable
import com.google.kpierudzki.driverassistant.obd.service.ObdManager
import com.google.kpierudzki.driverassistant.obd.service.obdmanager.IObdCommonListener
import com.google.kpierudzki.driverassistant.service.connector.ManagerConnectorType
import com.google.kpierudzki.driverassistant.service.helper.BluetoothAdapterState
import com.google.kpierudzki.driverassistant.service.manager.BaseServiceManager
import com.google.kpierudzki.driverassistant.util.MainThreadUtil

class EcoDrivingSensorBasedManager: BaseServiceManager, EcoDrivingCalculatorCallbacks, IEcoDrivingObservable,
        IGeoSampleListener, IObdCommonListener {

    private val BUFFER_LIMIT = 2000
    private val ecoDrivingCalculator: EcoDrivingSensorCalculator
    private var geoSampleObservable: IGeoSampleObservable? = null

    constructor(): super(arrayOf(ManagerConnectorType.EcoDrivingSensors)) {
        ecoDrivingCalculator = EcoDrivingSensorCalculator(
                App.getDatabase().ecoDrivingDao,
                this,
                BUFFER_LIMIT)
    }

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

    @WorkerThread
    private fun persistBuffers() {
        synchronized(this) {
            geoSampleObservable?.forcePersistBuffer(false)
            ecoDrivingCalculator.forcePersistBuffer()
        }
    }

    override fun onSpeedChanged(speed: Float) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onAccelerationChanged(acceleration: Float) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onAvgScoreChanged(score: Float) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun forcePersistBuffer(async: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onPermissionGranted() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onBluetoothAdapterStateChanged(state: BluetoothAdapterState) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onConnectionStateChanged(state: ObdManager.ConnectionState?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onDeviceMalfunction() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onNewData(newData: GeoSamplesSwappableData) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onGpsProviderStateChanged(state: GpsProviderState) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onRawLocation(location: Location) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}