package com.google.kpierudzki.driverassistant.common.service

import android.support.annotation.MainThread
import com.google.kpierudzki.driverassistant.common.connector.IPermissionRequirable
import com.google.kpierudzki.driverassistant.common.connector.IPersistableBuffer
import com.google.kpierudzki.driverassistant.obd.read.connector.IObdReadListener
import com.google.kpierudzki.driverassistant.obd.read.connector.IObdReadObservable
import com.google.kpierudzki.driverassistant.obd.service.ObdManager
import com.google.kpierudzki.driverassistant.obd.service.obdmanager.IObdCommonListener
import com.google.kpierudzki.driverassistant.service.connector.ManagerConnectorType
import com.google.kpierudzki.driverassistant.service.helper.BluetoothAdapterState
import com.google.kpierudzki.driverassistant.service.manager.BaseServiceManager
import com.google.kpierudzki.driverassistant.util.MainThreadUtil

abstract class ObdDataBasedManager : BaseServiceManager, IObdReadListener, IPersistableBuffer,
        IPermissionRequirable {

    protected var obdReadObservable: IObdReadObservable?

    constructor(obdReadObservable: IObdReadObservable?, types: Array<ManagerConnectorType>) : super(types) {
        this.obdReadObservable = obdReadObservable
    }

    @MainThread
    override fun onDestroy() {
        if (threadPool != null && !threadPool.isTerminating) {
            threadPool.execute {
                synchronized(this) {
                    obdReadObservable = null
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
            obdReadObservable?.forcePersistBuffer(false)
        }
    }

    override fun onNewDtcDetected() {
        //Ignore
    }

    override fun onBluetoothAdapterStateChanged(state: BluetoothAdapterState) {
        //Ignore
    }

    override fun onPermissionGranted() {
        obdReadObservable?.onPermissionGranted()
    }

    override fun onDeviceMalfunction() {
        notifyListenersWithData(0, { _: Int, type: IObdCommonListener ->
            type.onDeviceMalfunction()
        })
    }

    override fun onConnectionStateChanged(state: ObdManager.ConnectionState) {
        //Ignore
    }
}