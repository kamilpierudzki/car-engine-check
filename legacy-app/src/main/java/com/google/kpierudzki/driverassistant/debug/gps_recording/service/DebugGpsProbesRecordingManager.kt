package com.google.kpierudzki.driverassistant.debug.gps_recording.service

import android.location.Location
import android.support.annotation.MainThread
import android.support.annotation.WorkerThread
import com.google.kpierudzki.driverassistant.App
import com.google.kpierudzki.driverassistant.common.service.GeoDataBasedManager
import com.google.kpierudzki.driverassistant.debug.gps_recording.GpsProbesRecordingContract
import com.google.kpierudzki.driverassistant.debug.gps_recording.connector.IGpsProbesRecordingListener
import com.google.kpierudzki.driverassistant.debug.gps_recording.connector.IGpsProbesRecordingObservable
import com.google.kpierudzki.driverassistant.debug.gps_recording.database.GpsProbeRecordingEntity
import com.google.kpierudzki.driverassistant.geoSamples.connector.IGeoSampleListener
import com.google.kpierudzki.driverassistant.service.connector.IBaseManager
import com.google.kpierudzki.driverassistant.service.connector.ManagerConnectorType
import java.util.*

/**
 * Created by Kamil on 26.06.2017.
 */

class DebugGpsProbesRecordingManager : GeoDataBasedManager, IGpsProbesRecordingObservable {

    private val BUFFER_SIZE = 300
    private val buffer: MutableList<GpsProbeRecordingEntity>

    private var _currentStatus: GpsProbesRecordingContract.RecordStatus = GpsProbesRecordingContract.RecordStatus.NOT_RECORDING

    constructor() : super(null, arrayOf(ManagerConnectorType.GpsProbesRecording)) {
        buffer = ArrayList()
    }

    override fun addListener(connectorType: ManagerConnectorType, listener: IBaseManager.IBaseManagerListener) {
        super.addListener(connectorType, listener)
        notifyRecordStatusChanged()
        notifySamplesCountChanged()
    }

    override fun provideObservable(connectorType: ManagerConnectorType): IBaseManager.IBaseManagerObservable? {
        return this
    }

    @WorkerThread
    override fun onRawLocation(location: Location) {
        if (threadPool != null && !threadPool.isTerminating) {
            threadPool.execute {
                synchronized(this) {
                    if (_currentStatus == GpsProbesRecordingContract.RecordStatus.RECORDING) {
                        if (buffer.size < BUFFER_SIZE) {
                            addSampleToBuffer(location)
                        } else {
                            App.getDatabase().gpsProbeRecordingDao.addAll(buffer)
                            buffer.clear()
                            addSampleToBuffer(location)
                        }
                    }
                }
            }
        }
    }

    @WorkerThread
    private fun addSampleToBuffer(location: Location) {
        buffer.add(GpsProbeRecordingEntity(System.currentTimeMillis(), location))
        notifySamplesCountChanged()
    }

    @MainThread
    @WorkerThread
    private fun notifySamplesCountChanged() {
        if (threadPool != null && !threadPool.isTerminating) {
            threadPool.execute {
                notifyListenersWithData(
                        App.getDatabase().gpsProbeRecordingDao.probesCount + buffer.size,
                        { data: Int, type: IGpsProbesRecordingListener ->
                            type.onNewSamplesCount(data)
                        })
            }
        }
    }

    @MainThread
    override fun startRecord() {
        synchronized(this) {
            _currentStatus = GpsProbesRecordingContract.RecordStatus.RECORDING
            notifyRecordStatusChanged()
        }
    }

    @MainThread
    override fun stopRecord() {
        synchronized(this) {
            _currentStatus = GpsProbesRecordingContract.RecordStatus.NOT_RECORDING
            notifyRecordStatusChanged()
        }
    }

    @WorkerThread
    private fun persistBuffer() {
        synchronized(this) {
            if (!buffer.isEmpty()) {
                App.getDatabase().gpsProbeRecordingDao.addAll(buffer)
                buffer.clear()
            }
        }
    }

    @MainThread
    private fun notifyRecordStatusChanged() {
        if (threadPool != null && !threadPool.isTerminating) {
            threadPool.execute {
                notifyListenersWithData(
                        _currentStatus,
                        { data: GpsProbesRecordingContract.RecordStatus, type: IGpsProbesRecordingListener ->
                            type.onNewRecordStatus(data)
                        })
            }
        }
    }
}
