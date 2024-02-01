package com.google.kpierudzki.driverassistant.debug.obd_recording.service

import android.support.annotation.MainThread
import android.support.annotation.WorkerThread
import com.google.kpierudzki.driverassistant.App
import com.google.kpierudzki.driverassistant.common.service.ObdDataBasedManager
import com.google.kpierudzki.driverassistant.debug.obd_recording.ObdProbesRecordingContract
import com.google.kpierudzki.driverassistant.debug.obd_recording.connector.IObdProbesRecordingListener
import com.google.kpierudzki.driverassistant.debug.obd_recording.connector.IObdProbesRecordingObservable
import com.google.kpierudzki.driverassistant.debug.obd_recording.database.*
import com.google.kpierudzki.driverassistant.obd.datamodel.ObdParamType
import com.google.kpierudzki.driverassistant.obd.service.commandmodels.ObdCommandModel
import com.google.kpierudzki.driverassistant.service.connector.IBaseManager
import com.google.kpierudzki.driverassistant.service.connector.ManagerConnectorType
import java.util.*

/**
 * Created by Kamil on 17.09.2017.
 */

class DebugObdProbesRecordingManager : ObdDataBasedManager, IObdProbesRecordingObservable {

    private val BUFFER_SIZE = 500
    private val speedBuffer: MutableList<ObdSpeedProbeRecordingEntity>
    private val rpmBuffer: MutableList<ObdRpmProbeRecordingEntity>
    private val mafBuffer: MutableList<ObdMafProbeRecordingEntity>
    private val coolantTempBuffer: MutableList<ObdCoolantTempProbeRecordingEntity>
    private val loadBuffer: MutableList<ObdLoadProbeRecordingEntity>
    private val barometricPressBuffer: MutableList<ObdBarometricPressProbeRecordingEntity>
    private val oilTempBuffer: MutableList<ObdOilTempProbeRecordingEntity>
    private val ambientAirTempBuffer: MutableList<ObdAmbientAirTempProbeRecordingEntity>

    private var _currentStatus: ObdProbesRecordingContract.RecordStatus = ObdProbesRecordingContract.RecordStatus.NOT_RECORDING

    constructor() : super(null, arrayOf(ManagerConnectorType.ObdProbesRecording)) {
        speedBuffer = ArrayList()
        rpmBuffer = ArrayList()
        mafBuffer = ArrayList()
        coolantTempBuffer = ArrayList()
        loadBuffer = ArrayList()
        barometricPressBuffer = ArrayList()
        oilTempBuffer = ArrayList()
        ambientAirTempBuffer = ArrayList()
    }

    @MainThread
    override fun provideObservable(connectorType: ManagerConnectorType): IBaseManager.IBaseManagerObservable? {
        return this
    }

    override fun addListener(connectorType: ManagerConnectorType, listener: IBaseManager.IBaseManagerListener) {
        super.addListener(connectorType, listener)
        notifyRecordStatusChanged()
        notifySamplesCountChanged(ObdParamType.SPEED, speedBuffer.size)
        notifySamplesCountChanged(ObdParamType.ENGINE_RPM, rpmBuffer.size)
        notifySamplesCountChanged(ObdParamType.MAF, rpmBuffer.size)
        notifySamplesCountChanged(ObdParamType.COOLANT_TEMP, rpmBuffer.size)
        notifySamplesCountChanged(ObdParamType.ENGINE_LOAD, rpmBuffer.size)
        notifySamplesCountChanged(ObdParamType.BAROMETRIC_PRESSURE, rpmBuffer.size)
        notifySamplesCountChanged(ObdParamType.OIL_TEMP, rpmBuffer.size)
        notifySamplesCountChanged(ObdParamType.AMBIENT_AIR_TEMP, rpmBuffer.size)
    }

    @WorkerThread
    override fun onNewObdData(data: ObdCommandModel) {
        if (threadPool != null && !threadPool.isTerminating) {
            threadPool.execute {
                synchronized(this) {
                    if (_currentStatus == ObdProbesRecordingContract.RecordStatus.RECORDING) {
                        when (data.paramType) {
                            ObdParamType.SPEED -> if (speedBuffer.size > BUFFER_SIZE) {
                                addSampleToSpeedBuffer(data.value)
                            } else {
                                App.getDatabase().obdSpeedProbeRecordingDao.addAll(speedBuffer)
                                speedBuffer.clear()
                                addSampleToSpeedBuffer(data.value)
                            }
                            ObdParamType.ENGINE_RPM -> if (rpmBuffer.size > BUFFER_SIZE) {
                                addSampleToRpmBuffer(data.value)
                            } else {
                                App.getDatabase().obdRpmProbeRecordingDao.addAll(rpmBuffer)
                                rpmBuffer.clear()
                                addSampleToRpmBuffer(data.value)
                            }
                            ObdParamType.MAF -> if (mafBuffer.size > BUFFER_SIZE) {
                                addSampleToMafBuffer(data.value)
                            } else {
                                App.getDatabase().obdMafProbeRecordingDao.addAll(mafBuffer)
                                mafBuffer.clear()
                                addSampleToMafBuffer(data.value)
                            }
                            ObdParamType.COOLANT_TEMP -> if (coolantTempBuffer.size > BUFFER_SIZE) {
                                addSampleToCoolantTempBuffer(data.value)
                            } else {
                                App.getDatabase().obdCoolantTempProbeRecordingDao.addAll(coolantTempBuffer)
                                coolantTempBuffer.clear()
                                addSampleToCoolantTempBuffer(data.value)
                            }
                            ObdParamType.ENGINE_LOAD -> if (loadBuffer.size > BUFFER_SIZE) {
                                addSampleToLoadBuffer(data.value)
                            } else {
                                App.getDatabase().obdLoadProbeRecordingDao.addAll(loadBuffer)
                                loadBuffer.clear()
                                addSampleToLoadBuffer(data.value)
                            }
                            ObdParamType.BAROMETRIC_PRESSURE -> if (barometricPressBuffer.size > BUFFER_SIZE) {
                                addSampleToBarometricPressBuffer(data.value)
                            } else {
                                App.getDatabase().obdBarometricPressProbeRecordingDao.addAll(barometricPressBuffer)
                                barometricPressBuffer.clear()
                                addSampleToBarometricPressBuffer(data.value)
                            }
                            ObdParamType.OIL_TEMP -> if (oilTempBuffer.size > BUFFER_SIZE) {
                                addSampleToOilTempBuffer(data.value)
                            } else {
                                App.getDatabase().obdOilTempProbeRecordingDao.addAll(oilTempBuffer)
                                oilTempBuffer.clear()
                                addSampleToOilTempBuffer(data.value)
                            }
                            ObdParamType.AMBIENT_AIR_TEMP -> if (ambientAirTempBuffer.size > BUFFER_SIZE) {
                                addSampleToAmbientAirTempBuffer(data.value)
                            } else {
                                App.getDatabase().obdAmbientAirTempProbeRecordingDao.addAll(ambientAirTempBuffer)
                                ambientAirTempBuffer.clear()
                                addSampleToAmbientAirTempBuffer(data.value)
                            }
                        }
                    }
                }
            }
        }
    }

    @MainThread
    override fun startRecord() {
        if (threadPool != null && !threadPool.isTerminating)
            threadPool.execute {
                synchronized(this) {
                    _currentStatus = ObdProbesRecordingContract.RecordStatus.RECORDING
                    notifyRecordStatusChanged()
                }
            }
    }

    @MainThread
    override fun stopRecord() {
        threadPool.execute {
            synchronized(this) {
                forcePersistBuffer()
                _currentStatus = ObdProbesRecordingContract.RecordStatus.NOT_RECORDING
                notifyRecordStatusChanged()
            }
        }
    }

    @MainThread
    fun forcePersistBuffer() {
        if (threadPool != null && !threadPool.isTerminating) {
            threadPool.execute { this.persistBuffers() }
        }
    }

    @WorkerThread
    override fun persistBuffers() {
        synchronized(this) {
            if (!speedBuffer.isEmpty()) {
                App.getDatabase().obdSpeedProbeRecordingDao.addAll(speedBuffer)
                speedBuffer.clear()
            }

            if (!rpmBuffer.isEmpty()) {
                App.getDatabase().obdRpmProbeRecordingDao.addAll(rpmBuffer)
                rpmBuffer.clear()
            }

            if (!mafBuffer.isEmpty()) {
                App.getDatabase().obdMafProbeRecordingDao.addAll(mafBuffer)
                mafBuffer.clear()
            }

            if (!coolantTempBuffer.isEmpty()) {
                App.getDatabase().obdCoolantTempProbeRecordingDao.addAll(coolantTempBuffer)
                coolantTempBuffer.clear()
            }

            if (!loadBuffer.isEmpty()) {
                App.getDatabase().obdLoadProbeRecordingDao.addAll(loadBuffer)
                loadBuffer.clear()
            }

            if (!barometricPressBuffer.isEmpty()) {
                App.getDatabase().obdBarometricPressProbeRecordingDao.addAll(barometricPressBuffer)
                barometricPressBuffer.clear()
            }

            if (!oilTempBuffer.isEmpty()) {
                App.getDatabase().obdOilTempProbeRecordingDao.addAll(oilTempBuffer)
                oilTempBuffer.clear()
            }

            if (!ambientAirTempBuffer.isEmpty()) {
                App.getDatabase().obdAmbientAirTempProbeRecordingDao.addAll(ambientAirTempBuffer)
                ambientAirTempBuffer.clear()
            }
        }
    }

    @MainThread
    private fun notifyRecordStatusChanged() {
        if (threadPool != null && !threadPool.isTerminating) {
            threadPool.execute {
                notifyListenersWithData(
                        _currentStatus,
                        { data: ObdProbesRecordingContract.RecordStatus, type: IObdProbesRecordingListener ->
                            type.onNewObdProbesRecordStatus(data)
                        })
            }
        }
    }

    @MainThread
    @WorkerThread
    private fun notifySamplesCountChanged(paramType: ObdParamType, bufferSize: Int) {
        if (threadPool != null && !threadPool.isTerminating) {
            threadPool.execute {
                synchronized(this) {
                    listeners.mapNotNull { it as? IObdProbesRecordingListener }.forEach { listener ->
                        val databaseCount = when (paramType) {
                            ObdParamType.SPEED -> App.getDatabase().obdSpeedProbeRecordingDao.probesCount
                            ObdParamType.ENGINE_RPM -> App.getDatabase().obdRpmProbeRecordingDao.probesCount
                            ObdParamType.MAF -> App.getDatabase().obdMafProbeRecordingDao.probesCount
                            ObdParamType.COOLANT_TEMP -> App.getDatabase().obdCoolantTempProbeRecordingDao.probesCount
                            ObdParamType.ENGINE_LOAD -> App.getDatabase().obdLoadProbeRecordingDao.probesCount
                            ObdParamType.BAROMETRIC_PRESSURE -> App.getDatabase().obdBarometricPressProbeRecordingDao.probesCount
                            ObdParamType.OIL_TEMP -> App.getDatabase().obdOilTempProbeRecordingDao.probesCount
                            ObdParamType.AMBIENT_AIR_TEMP -> App.getDatabase().obdAmbientAirTempProbeRecordingDao.probesCount
                            else -> 0
                        }

                        listener.onNewSamplesCount(paramType, databaseCount + bufferSize)
                    }
                }
            }
        }
    }

    @WorkerThread
    private fun addSampleToSpeedBuffer(newValue: Float) {
        speedBuffer.add(ObdSpeedProbeRecordingEntity(System.currentTimeMillis(), newValue))
        notifySamplesCountChanged(ObdParamType.SPEED, speedBuffer.size)
    }

    @WorkerThread
    private fun addSampleToRpmBuffer(newValue: Float) {
        rpmBuffer.add(ObdRpmProbeRecordingEntity(System.currentTimeMillis(), newValue))
        notifySamplesCountChanged(ObdParamType.ENGINE_RPM, rpmBuffer.size)
    }

    @WorkerThread
    private fun addSampleToMafBuffer(newValue: Float) {
        mafBuffer.add(ObdMafProbeRecordingEntity(System.currentTimeMillis(), newValue))
        notifySamplesCountChanged(ObdParamType.MAF, mafBuffer.size)
    }

    @WorkerThread
    private fun addSampleToCoolantTempBuffer(newValue: Float) {
        coolantTempBuffer.add(ObdCoolantTempProbeRecordingEntity(System.currentTimeMillis(), newValue))
        notifySamplesCountChanged(ObdParamType.COOLANT_TEMP, coolantTempBuffer.size)
    }

    @WorkerThread
    private fun addSampleToLoadBuffer(newValue: Float) {
        loadBuffer.add(ObdLoadProbeRecordingEntity(System.currentTimeMillis(), newValue))
        notifySamplesCountChanged(ObdParamType.ENGINE_LOAD, loadBuffer.size)
    }

    @WorkerThread
    private fun addSampleToBarometricPressBuffer(newValue: Float) {
        barometricPressBuffer.add(ObdBarometricPressProbeRecordingEntity(System.currentTimeMillis(), newValue))
        notifySamplesCountChanged(ObdParamType.BAROMETRIC_PRESSURE, barometricPressBuffer.size)
    }

    @WorkerThread
    private fun addSampleToOilTempBuffer(newValue: Float) {
        oilTempBuffer.add(ObdOilTempProbeRecordingEntity(System.currentTimeMillis(), newValue))
        notifySamplesCountChanged(ObdParamType.OIL_TEMP, oilTempBuffer.size)
    }

    @WorkerThread
    private fun addSampleToAmbientAirTempBuffer(newValue: Float) {
        ambientAirTempBuffer.add(ObdAmbientAirTempProbeRecordingEntity(System.currentTimeMillis(), newValue))
        notifySamplesCountChanged(ObdParamType.AMBIENT_AIR_TEMP, ambientAirTempBuffer.size)
    }

    override fun disconnect() {
        // Ignore
    }
}
