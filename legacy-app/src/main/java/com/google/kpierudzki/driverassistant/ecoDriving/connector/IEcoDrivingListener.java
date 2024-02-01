package com.google.kpierudzki.driverassistant.ecoDriving.connector;

import com.google.kpierudzki.driverassistant.ecoDriving.EcoDrivingContract;
import com.google.kpierudzki.driverassistant.geoSamples.connector.GpsProviderState;
import com.google.kpierudzki.driverassistant.service.connector.IBaseManager;

/**
 * Created by Kamil on 03.07.2017.
 */

public interface IEcoDrivingListener extends IBaseManager.IBaseManagerListener {
    void onAccelerationChanged(float acceleration);

    void onAvgScoreChanged(float score);

    void onSpeedChanged(float speed);

    void onGpsProviderStateChanged(GpsProviderState state);

    void onDataProviderChanged(EcoDrivingContract.EcoDrivingDataProvider provider);
}
