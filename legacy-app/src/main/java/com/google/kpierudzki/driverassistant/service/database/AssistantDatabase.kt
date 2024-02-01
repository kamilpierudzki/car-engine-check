package com.google.kpierudzki.driverassistant.service.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import com.google.kpierudzki.driverassistant.App
import com.google.kpierudzki.driverassistant.debug.gps_recording.database.GpsProbeRecordingDao
import com.google.kpierudzki.driverassistant.debug.gps_recording.database.GpsProbeRecordingEntity
import com.google.kpierudzki.driverassistant.debug.obd_recording.database.*
import com.google.kpierudzki.driverassistant.dtc.database.DtcDao
import com.google.kpierudzki.driverassistant.dtc.database.DtcEntity
import com.google.kpierudzki.driverassistant.ecoDriving.database.EcoDrivingDao
import com.google.kpierudzki.driverassistant.ecoDriving.database.EcoDrivingEntity
import com.google.kpierudzki.driverassistant.geoSamples.database.GeoSamplesDao
import com.google.kpierudzki.driverassistant.geoSamples.database.GeoSamplesEntity
import com.google.kpierudzki.driverassistant.geoSamples.database.GeoSamplesTracksEntity
import com.google.kpierudzki.driverassistant.history.calendar.database.HistoryCalendarDao
import com.google.kpierudzki.driverassistant.history.calendar.database.HistoryTranslationEntity
import com.google.kpierudzki.driverassistant.obd.database.MruDao
import com.google.kpierudzki.driverassistant.obd.database.MruEntity
import com.google.kpierudzki.driverassistant.obd.database.ObdParamsDao
import com.google.kpierudzki.driverassistant.obd.database.ObdParamsEntity

/**
 * Created by Kamil on 15.07.2017.
 */

@Database(entities = arrayOf(
        EcoDrivingEntity::class,
        GeoSamplesTracksEntity::class,
        GeoSamplesEntity::class,
        HistoryTranslationEntity::class,
        MruEntity::class,
        ObdParamsEntity::class,
        GpsProbeRecordingEntity::class,
        ObdSpeedProbeRecordingEntity::class,
        ObdRpmProbeRecordingEntity::class,
        ObdMafProbeRecordingEntity::class,
        ObdCoolantTempProbeRecordingEntity::class,
        ObdLoadProbeRecordingEntity::class,
        ObdBarometricPressProbeRecordingEntity::class,
        ObdOilTempProbeRecordingEntity::class,
        ObdAmbientAirTempProbeRecordingEntity::class,
        DtcEntity::class),
        version = 1)
abstract class AssistantDatabase : RoomDatabase() {

    abstract val ecoDrivingDao: EcoDrivingDao

    abstract val geoSamplesDao: GeoSamplesDao

    abstract val historyCalendarDao: HistoryCalendarDao

    abstract val mruDao: MruDao

    abstract val obdParamsDao: ObdParamsDao

    abstract val gpsProbeRecordingDao: GpsProbeRecordingDao

    abstract val obdSpeedProbeRecordingDao: ObdSpeedProbeRecordingDao

    abstract val obdRpmProbeRecordingDao: ObdRpmProbeRecordingDao

    abstract val obdMafProbeRecordingDao: ObdMafProbeRecordingDao

    abstract val obdCoolantTempProbeRecordingDao: ObdCoolantTempProbeRecordingDao

    abstract val obdLoadProbeRecordingDao: ObdLoadProbeRecordingDao

    abstract val obdBarometricPressProbeRecordingDao: ObdBarometricPressProbeRecordingDao

    abstract val obdOilTempProbeRecordingDao: ObdOilTempProbeRecordingDao

    abstract val obdAmbientAirTempProbeRecordingDao: ObdAmbientAirTempProbeRecordingDao

    abstract val dtcDao: DtcDao

    companion object {

        private lateinit var INSTANCE: AssistantDatabase

        fun getInstance(): AssistantDatabase {
            if (!::INSTANCE.isInitialized) {
                synchronized(this) {
                    INSTANCE = Room.databaseBuilder(
                            App.getAppContext(),
                            AssistantDatabase::class.java,
                            AssistantDatabase.DATABASE_FILENAME)
                            .build()
                }
            }

            return INSTANCE;
        }

        const val DATABASE_FILENAME = "assistant_database"
        const val DEMO_DATABASE_FILENAME = "demo_assistant_database"
    }
}
