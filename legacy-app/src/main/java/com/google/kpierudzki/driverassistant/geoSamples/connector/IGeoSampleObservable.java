package com.google.kpierudzki.driverassistant.geoSamples.connector;

import com.google.kpierudzki.driverassistant.common.connector.IPermissionRequirable;
import com.google.kpierudzki.driverassistant.common.connector.IPersistableBuffer;
import com.google.kpierudzki.driverassistant.service.connector.IBaseManager;

import java.util.List;

/**
 * Created by Kamil on 02.08.2017.
 */

public interface IGeoSampleObservable extends IPersistableBuffer, IPermissionRequirable, IBaseManager.IBaseManagerObservable {
    void onTrackRemoved(List<Long> removedTracks);
}
