package com.google.kpierudzki.driverassistant.geoSamples.service

import android.content.Context
import android.location.Location
import android.os.Bundle
import android.support.annotation.MainThread
import android.support.annotation.WorkerThread
import com.google.kpierudzki.driverassistant.App
import com.google.kpierudzki.driverassistant.GlobalConfig
import com.google.kpierudzki.driverassistant.common.model.Coordinate
import com.google.kpierudzki.driverassistant.geoSamples.connector.GeoSamplesSwappableData
import com.google.kpierudzki.driverassistant.geoSamples.connector.GpsProviderState
import com.google.kpierudzki.driverassistant.geoSamples.connector.IGeoSampleListener
import com.google.kpierudzki.driverassistant.geoSamples.connector.IGeoSampleObservable
import com.google.kpierudzki.driverassistant.geoSamples.database.GeoSamplesDao
import com.google.kpierudzki.driverassistant.geoSamples.database.GeoSamplesEntity
import com.google.kpierudzki.driverassistant.geoSamples.database.GeoSamplesTracksEntity
import com.google.kpierudzki.driverassistant.service.connector.IBaseManager
import com.google.kpierudzki.driverassistant.service.connector.ManagerConnectorType
import com.google.kpierudzki.driverassistant.service.manager.BaseServiceManager
import com.google.kpierudzki.driverassistant.service.mock.location.LocationRepository
import com.google.kpierudzki.driverassistant.util.MainThreadUtil
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by Kamil on 13.01.2018.
 */
class GeoSamplesManager(context: Context) : BaseServiceManager(arrayOf(ManagerConnectorType.GeoSamples)),
        IGeoSampleObservable, LocationListener {

    private val GEO_SAMPLES_BUFFER_LIMIT: Int = 300

    private lateinit var _currentTrackInfo: CurrentTrackInfo
    private var _buffer: ArrayList<GeoSamplesEntity> = arrayListOf()
    private var _locationRepository: LocationRepository = LocationRepository(context, this)
    private var _currentState: GpsProviderState = GpsProviderState.Disabled
    private var _currentTrackRemoved: Boolean = false

    override fun onDestroy() {
        threadPool?.let {
            if (!it.isTerminating) {
                it.execute({
                    synchronized(this) {
                        persistBuffer();
                        MainThreadUtil.post({
                            _locationRepository.onDestroy()
                            super.onDestroy()
                        })
                    }
                })
            }
        }
    }

    override fun addListener(connectorType: ManagerConnectorType, listener: IBaseManager.IBaseManagerListener) {
        super.addListener(connectorType, listener)
        notifyWithProviderState(_currentState, listener)
    }

    override fun provideObservable(connectorType: ManagerConnectorType): IBaseManager.IBaseManagerObservable? {
        return this
    }

    override fun onFirstListenerAdded(listener: IBaseManager.IBaseManagerListener) {
        _locationRepository.requestLocationUpdates()
    }

    override fun onLastListenerRemoved(listener: IBaseManager.IBaseManagerListener) {
        _locationRepository.removeUpdates()
    }

    override fun onProviderNotSupported() {
        _currentState = GpsProviderState.NotSupported
        notifyListenersWithProviderState(_currentState)
    }

    override fun onPermissionGranted() {
        _locationRepository.removeUpdates()
        _locationRepository.requestLocationUpdates()
    }

    override fun onLocationChanged(location: Location) {
        threadPool?.let {
            if (!it.isTerminating) {
                it.execute({
                    synchronized(this) {
                        val geoSamplesDao = App.getDatabase().geoSamplesDao
                        handleUpdate(geoSamplesDao, location)

                        val offset = System.currentTimeMillis() - _currentTrackInfo.startTimeInMillis

                        if (_buffer.size < GEO_SAMPLES_BUFFER_LIMIT) {
                            _buffer.add(GeoSamplesEntity(
                                    _currentTrackInfo.trackId,
                                    Coordinate(location),
                                    offset,
                                    location.speed))
                        } else {
                            geoSamplesDao.addAll(_buffer)
                            _buffer.clear()
                        }

                        listeners.filter { it is IGeoSampleListener }
                                .map { it as IGeoSampleListener }
                                .forEach {
                                    it.onNewData(GeoSamplesSwappableData(
                                            _currentTrackInfo.trackId,
                                            offset,
                                            location.speed))
                                    it.onRawLocation(location)
                                }
                    }
                })
            }
        }
    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
        // Ignore
    }

    override fun onProviderEnabled(provider: String) {
        _currentState = GpsProviderState.Enabled
        notifyListenersWithProviderState(_currentState)
    }

    override fun onProviderDisabled(provider: String) {
        _currentState = GpsProviderState.Disabled
        notifyListenersWithProviderState(_currentState)
    }

    override fun forcePersistBuffer(async: Boolean) {
        if (async) {
            threadPool?.let {
                if (!it.isTerminating) it.execute({ persistBuffer() })
            }
        } else {
            persistBuffer()
        }
    }

    @WorkerThread
    private fun persistBuffer() {
        synchronized(this) {
            if (!_buffer.isEmpty()) {
                App.getDatabase().geoSamplesDao.addAll(_buffer)
                _buffer.clear()
            }
        }
    }

    private fun notifyListenersWithProviderState(state: GpsProviderState) {
        threadPool?.let {
            if (!it.isTerminating) {
                it.execute({
                    synchronized(this) {
                        listeners.filter { it is IGeoSampleListener }
                                .map { it as IGeoSampleListener }
                                .forEach { it.onGpsProviderStateChanged(state) }
                    }
                })
            }
        }
    }

    @WorkerThread
    override fun onTrackRemoved(removedTracks: List<Long>) {
        synchronized(this) {
            _currentTrackRemoved = removedTracks.firstOrNull { id ->
                _currentTrackInfo.trackId == id
            } != null
        }
    }

    private fun handleUpdate(geoSamplesDao: GeoSamplesDao, location: Location) {
        if (_currentTrackRemoved) {
            _currentTrackRemoved = false
            createNewTrack(
                    geoSamplesDao,
                    location,
                    System.currentTimeMillis())
        }

        if (GlobalConfig.DEMO_MODE)
            demoDetectNewTrack(geoSamplesDao, location)
        else
            detectNewTrack(geoSamplesDao, location)
    }

    private fun createNewTrack(geoSamplesDao: GeoSamplesDao, location: Location, currentTimeInMillis: Long) {
        geoSamplesDao.addTrack(GeoSamplesTracksEntity(currentTimeInMillis, Coordinate(location)))
        val newTrack = geoSamplesDao.lastTrack
        _currentTrackInfo.trackId = newTrack.trackID
    }

    @WorkerThread
    private fun detectNewTrack(geoSamplesDao: GeoSamplesDao, location: Location) {
        if (!::_currentTrackInfo.isInitialized) {
            _currentTrackInfo = try {
                val lastTrack = geoSamplesDao.lastTrack
                val lastSample = geoSamplesDao.getLastSampleForTrackId(lastTrack.trackID)
                CurrentTrackInfo(
                        lastTrack.trackID,
                        lastTrack.startTime + lastSample.offset,
                        lastTrack.startTime)
            } catch (e: Exception) {
                CurrentTrackInfo(
                        -1,
                        0,
                        System.currentTimeMillis())
            }
        }

        val currentTimeInMillis = System.currentTimeMillis()
        val difference = currentTimeInMillis - _currentTrackInfo.lastSampleTimeInMillis

        if (difference > TimeUnit.HOURS.toMillis(1)) {
            // Recognized new track
            createNewTrack(geoSamplesDao, location, currentTimeInMillis)
        }

        _currentTrackInfo.lastSampleTimeInMillis = currentTimeInMillis
    }

    @WorkerThread
    private fun demoDetectNewTrack(geoSamplesDao: GeoSamplesDao, location: Location) {
        if (!::_currentTrackInfo.isInitialized) {
            _currentTrackInfo = try {
                val lastTrack = geoSamplesDao.lastTrack
                val lastSample = geoSamplesDao.getLastSampleForTrackId(lastTrack.trackID)
                CurrentTrackInfo(
                        lastTrack.trackID,
                        lastTrack.startTime + lastSample.offset,
                        lastTrack.startTime)
            } catch (e: Exception) {
                CurrentTrackInfo(
                        -1,
                        0,
                        System.currentTimeMillis())
            }

            createNewTrack(geoSamplesDao, location, System.currentTimeMillis())
        }
    }

    @MainThread
    private fun notifyWithProviderState(state: GpsProviderState, listener: IBaseManager.IBaseManagerListener) {
        (listener as? IGeoSampleListener)?.onGpsProviderStateChanged(state)
    }
}

private class CurrentTrackInfo(var trackId: Long, var lastSampleTimeInMillis: Long, var startTimeInMillis: Long)