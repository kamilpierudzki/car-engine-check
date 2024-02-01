package com.google.kpierudzki.driverassistant.ecoDriving.service

import android.support.annotation.MainThread
import android.support.annotation.WorkerThread
import com.google.kpierudzki.driverassistant.App
import com.google.kpierudzki.driverassistant.common.service.GeoDataBasedManager
import com.google.kpierudzki.driverassistant.ecoDriving.EcoDrivingContract
import com.google.kpierudzki.driverassistant.ecoDriving.connector.IEcoDrivingListener
import com.google.kpierudzki.driverassistant.ecoDriving.connector.IEcoDrivingObservable
import com.google.kpierudzki.driverassistant.ecoDriving.helper.EcoDrivingCalculatorCallbacks
import com.google.kpierudzki.driverassistant.ecoDriving.helper.EcoDrivingGpsCalculator
import com.google.kpierudzki.driverassistant.geoSamples.connector.GeoSamplesSwappableData
import com.google.kpierudzki.driverassistant.geoSamples.connector.GpsProviderState
import com.google.kpierudzki.driverassistant.geoSamples.connector.IGeoSampleListener
import com.google.kpierudzki.driverassistant.geoSamples.connector.IGeoSampleObservable
import com.google.kpierudzki.driverassistant.obd.service.ObdManager
import com.google.kpierudzki.driverassistant.obd.service.obdmanager.IObdCommonListener
import com.google.kpierudzki.driverassistant.service.connector.IBaseManager
import com.google.kpierudzki.driverassistant.service.connector.ManagerConnectorType
import com.google.kpierudzki.driverassistant.service.helper.BluetoothAdapterState

/**
 * Created by Kamil on 03.07.2017.
 */

class EcoDrivingGpsBasedManager : GeoDataBasedManager, IEcoDrivingObservable, EcoDrivingCalculatorCallbacks, IObdCommonListener {

    private val ECO_DRIVING_PROBES_BUFFER_LIMIT = 300
    private val ecoDrivingCalculator: EcoDrivingGpsCalculator
    private var isManagerAllowedToWork = false
    private var _currentState: GpsProviderState? = null

    constructor(geoSampleObservable: IGeoSampleObservable?)
            : super(geoSampleObservable, arrayOf(ManagerConnectorType.EcoDrivingGps)) {
        _currentState = GpsProviderState.Disabled
        ecoDrivingCalculator = EcoDrivingGpsCalculator(
                App.getDatabase().ecoDrivingDao,
                this,
                ECO_DRIVING_PROBES_BUFFER_LIMIT)
    }

    @MainThread
    override fun addListener(connectorType: ManagerConnectorType, listener: IBaseManager.IBaseManagerListener) {
        super.addListener(connectorType, listener)
        notifyListenerWithConnectionStateChanged(listener)
        notifyWithProviderState(listener)
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
        _currentState = state
        notifyListenersWithData(state, { data: GpsProviderState, type: IEcoDrivingListener ->
            type.onGpsProviderStateChanged(data)
        })
    }

    override fun persistBuffers() {
        synchronized(this) {
            super.persistBuffers()
            ecoDrivingCalculator.forcePersistBuffer()
        }
    }

    override fun onSpeedChanged(speed: Float) {
        notifyListenersWithData(speed, { data: Float, type: IEcoDrivingListener ->
            type.onSpeedChanged(data)
        })
    }

    override fun onAccelerationChanged(acceleration: Float) {
        notifyListenersWithData(acceleration, { data: Float, type: IEcoDrivingListener ->
            type.onAccelerationChanged(data)
        })
    }

    override fun onAvgScoreChanged(score: Float) {
        notifyListenersWithData(score, { data: Float, type: IEcoDrivingListener ->
            type.onAvgScoreChanged(data)
        })
    }

    @WorkerThread
    override fun onConnectionStateChanged(state: ObdManager.ConnectionState) {
        synchronized(this) {
            when (state) {
                ObdManager.ConnectionState.Connected -> isManagerAllowedToWork = false
                else -> isManagerAllowedToWork = true
            }
            notifyListenersWithConnectionStateChanged()
        }
    }

    override fun onDeviceMalfunction() {
        notifyListenersWithData(0, { _, type: IObdCommonListener ->
            type.onDeviceMalfunction()
        })
    }

    @MainThread
    @WorkerThread
    private fun notifyListenersWithConnectionStateChanged() {
        if (threadPool != null && !threadPool.isTerminating)
            threadPool.execute {
                synchronized(this) {
                    val provider = if (isManagerAllowedToWork) {
                        EcoDrivingContract.EcoDrivingDataProvider.Gps
                    } else {
                        EcoDrivingContract.EcoDrivingDataProvider.Obd
                    }

                    notifyListenersWithData(
                            provider,
                            { data: EcoDrivingContract.EcoDrivingDataProvider,
                              type: IEcoDrivingListener ->
                                type.onDataProviderChanged(data)
                            })
                }
            }
    }

    @MainThread
    @WorkerThread
    private fun notifyListenerWithConnectionStateChanged(listener: IBaseManager.IBaseManagerListener) {
        if (threadPool != null && !threadPool.isTerminating)
            threadPool.execute {
                synchronized(this) {
                    val provider = if (isManagerAllowedToWork) {
                        EcoDrivingContract.EcoDrivingDataProvider.Gps
                    } else {
                        EcoDrivingContract.EcoDrivingDataProvider.Obd
                    }

                    (listener as? IEcoDrivingListener)?.onDataProviderChanged(provider)
                }
            }
    }

    @MainThread
    private fun notifyWithProviderState(listener: IBaseManager.IBaseManagerListener) {
        (listener as? IEcoDrivingListener)?.onGpsProviderStateChanged(_currentState)
    }

    override fun onBluetoothAdapterStateChanged(state: BluetoothAdapterState) {
        //Ignore
    }
}
