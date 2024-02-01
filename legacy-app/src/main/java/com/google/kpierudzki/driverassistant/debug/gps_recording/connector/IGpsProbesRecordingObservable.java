package com.google.kpierudzki.driverassistant.debug.gps_recording.connector;

import com.google.kpierudzki.driverassistant.common.connector.IPersistableBuffer;
import com.google.kpierudzki.driverassistant.service.connector.IBaseManager;

/**
 * Created by Kamil on 28.06.2017.
 */

public interface IGpsProbesRecordingObservable extends IPersistableBuffer, IBaseManager.IBaseManagerObservable {
    void startRecord();

    void stopRecord();
}
