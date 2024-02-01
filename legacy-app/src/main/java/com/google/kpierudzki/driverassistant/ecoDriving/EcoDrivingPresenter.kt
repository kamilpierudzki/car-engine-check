package com.google.kpierudzki.driverassistant.ecoDriving

import com.google.kpierudzki.driverassistant.ecoDriving.connector.IEcoDrivingObservable
import com.google.kpierudzki.driverassistant.ecoDriving.usecase.EcoDrivingDbUseCase
import com.google.kpierudzki.driverassistant.geoSamples.connector.GpsProviderState
import com.google.kpierudzki.driverassistant.service.connector.ManagerConnectorType
import com.google.kpierudzki.driverassistant.service.helper.ServiceBindHelper
import com.google.kpierudzki.driverassistant.util.MainThreadUtil
import java.lang.ref.WeakReference

/**
 * Created by Kamil on 25.06.2017.
 */

class EcoDrivingPresenter(view: EcoDrivingContract.View) : EcoDrivingContract.Presenter, EcoDrivingDbUseCase.Callback {

    private val viewRef: WeakReference<EcoDrivingContract.View> = WeakReference(view)
    private lateinit var serviceBindHelperRef: WeakReference<ServiceBindHelper>
    private lateinit var ecoDrivingGpsObservableRef: WeakReference<IEcoDrivingObservable>
    private var dbUseCase: EcoDrivingDbUseCase? = null

    init {
        view.setPresenter(this)
    }

    override fun start() {
        dbUseCase = EcoDrivingDbUseCase(this)
        bindConnector()
    }

    override fun stop() {
        unbindConnector()

        dbUseCase?.onDestroy()
        dbUseCase = null
    }

    override fun provideLastNSamplesForParam(N: Int, parameter: EcoDrivingContract.EcoDrivingParameter) {
        dbUseCase?.provideLastNSamplesForParam(N, parameter)
    }

    override fun onPermissionGranted() {
        ecoDrivingGpsObservableRef.get()?.onPermissionGranted()
    }

    override fun onAccelerationChanged(acceleration: Float) {
        MainThreadUtil.post { viewRef.get()?.updateChart(acceleration) }
    }

    override fun onAvgScoreChanged(score: Float) {
        MainThreadUtil.post { viewRef.get()?.updateScoreClock(score) }
    }

    override fun onSpeedChanged(speed: Float) {
        MainThreadUtil.post { viewRef.get()?.updateSpeedClock(speed) }
    }

    override fun onGpsProviderStateChanged(state: GpsProviderState) {
        MainThreadUtil.post { viewRef.get()?.updateGpsState(state) }
    }

    override fun onDataProviderChanged(provider: EcoDrivingContract.EcoDrivingDataProvider) {
        MainThreadUtil.post { viewRef.get()?.onDataProviderChanged(provider) }
    }

    override fun onLastDataOfParam(data: List<Float>, parameter: EcoDrivingContract.EcoDrivingParameter) {
        MainThreadUtil.post { viewRef.get()?.onLastDataOfParam(data, parameter) }
    }

    private fun bindConnector() {
        serviceBindHelperRef = WeakReference(ServiceBindHelper { connector ->
            connector.addListener(ManagerConnectorType.EcoDrivingGps, this@EcoDrivingPresenter)
            connector.addListener(ManagerConnectorType.EcoDrivingObd, this@EcoDrivingPresenter)
            ecoDrivingGpsObservableRef = WeakReference(
                    connector.provideObservable(ManagerConnectorType.EcoDrivingGps)
                            as IEcoDrivingObservable)
            viewRef.get()?.onPresenterReady(this)
        })
    }

    private fun unbindConnector() {
        serviceBindHelperRef.get()?.also { binder ->
            binder.serviceConnector?.also { connector ->
                connector.removeListener(ManagerConnectorType.EcoDrivingGps, this@EcoDrivingPresenter)
                connector.removeListener(ManagerConnectorType.EcoDrivingObd, this@EcoDrivingPresenter)
            }
            binder.onDestroy()
        }
        serviceBindHelperRef.clear()
    }
}
