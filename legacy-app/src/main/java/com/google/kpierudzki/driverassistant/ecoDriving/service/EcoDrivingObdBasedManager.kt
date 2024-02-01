package com.google.kpierudzki.driverassistant.ecoDriving.service

import android.location.Location
import android.support.annotation.MainThread
import android.support.annotation.WorkerThread

import com.google.kpierudzki.driverassistant.App
import com.google.kpierudzki.driverassistant.common.service.ObdDataBasedManager
import com.google.kpierudzki.driverassistant.ecoDriving.connector.IEcoDrivingListener
import com.google.kpierudzki.driverassistant.ecoDriving.connector.IEcoDrivingObservable
import com.google.kpierudzki.driverassistant.ecoDriving.helper.EcoDrivingCalculatorCallbacks
import com.google.kpierudzki.driverassistant.ecoDriving.helper.EcoDrivingObdCalculator
import com.google.kpierudzki.driverassistant.geoSamples.connector.GeoSamplesSwappableData
import com.google.kpierudzki.driverassistant.geoSamples.connector.GpsProviderState
import com.google.kpierudzki.driverassistant.geoSamples.connector.IGeoSampleListener
import com.google.kpierudzki.driverassistant.obd.datamodel.ObdParamType
import com.google.kpierudzki.driverassistant.obd.read.connector.IObdReadListener
import com.google.kpierudzki.driverassistant.obd.read.connector.IObdReadObservable
import com.google.kpierudzki.driverassistant.obd.service.ObdManager
import com.google.kpierudzki.driverassistant.obd.service.commandmodels.ObdCommandModel
import com.google.kpierudzki.driverassistant.obd.service.obdmanager.IObdCommonListener
import com.google.kpierudzki.driverassistant.service.connector.IBaseManager
import com.google.kpierudzki.driverassistant.service.connector.ManagerConnectorType
import com.google.kpierudzki.driverassistant.service.helper.BluetoothAdapterState
import com.google.kpierudzki.driverassistant.service.manager.BaseServiceManager
import com.google.kpierudzki.driverassistant.util.MainThreadUtil
import java8.util.function.Consumer

import java8.util.stream.StreamSupport

/**
 * Created by Kamil on 05.10.2017.
 */

class EcoDrivingObdBasedManager : ObdDataBasedManager, IEcoDrivingObservable, IGeoSampleListener, EcoDrivingCalculatorCallbacks {

    val ECO_DRIVING_PROBES_BUFFER_LIMIT = 300
    private val ecoDrivingCalculator: EcoDrivingObdCalculator
    private var isManagerAllowedToWork = false

    constructor(obdReadObservable: IObdReadObservable?)
            : super(obdReadObservable, arrayOf(ManagerConnectorType.EcoDrivingObd)) {
        ecoDrivingCalculator = EcoDrivingObdCalculator(
                App.getDatabase().ecoDrivingDao,
                this,
                ECO_DRIVING_PROBES_BUFFER_LIMIT)
    }

    @MainThread
    override fun provideObservable(connectorType: ManagerConnectorType): IBaseManager.IBaseManagerObservable? {
        return this
    }

    @WorkerThread
    override fun onNewData(newData: GeoSamplesSwappableData) {
        if (isManagerAllowedToWork) {
            if (threadPool != null && !threadPool.isTerminating)
                threadPool.execute { ecoDrivingCalculator.updateGeoData(newData) }
        }
    }

    override fun onGpsProviderStateChanged(state: GpsProviderState) {
        //Ignore
    }

    override fun onRawLocation(location: Location) {
        // Ignore
    }

    @WorkerThread
    override fun onConnectionStateChanged(state: ObdManager.ConnectionState) {
        synchronized(this) {
            when (state) {
                ObdManager.ConnectionState.Connected -> isManagerAllowedToWork = true
                else -> isManagerAllowedToWork = false
            }
        }
    }

    @WorkerThread
    override fun onNewObdData(data: ObdCommandModel) {
        if (isManagerAllowedToWork && data.paramType == ObdParamType.SPEED)
            ecoDrivingCalculator.onNewSpeed(data.value)
    }

    override fun onSpeedChanged(speed: Float) {
        notifyListenersWithData(speed, {data: Float, type: IEcoDrivingListener ->
            type.onSpeedChanged(data)
        })
    }

    override fun onAccelerationChanged(acceleration: Float) {
        notifyListenersWithData(acceleration, {data: Float, type: IEcoDrivingListener ->
            type.onAccelerationChanged(data)
        })
    }

    override fun onAvgScoreChanged(score: Float) {
        notifyListenersWithData(score, {data: Float, type: IEcoDrivingListener ->
            type.onAvgScoreChanged(data)
        })
    }

    override fun persistBuffers() {
        synchronized(this) {
            super.persistBuffers()
            ecoDrivingCalculator.forcePersistBuffer()
        }
    }
}
