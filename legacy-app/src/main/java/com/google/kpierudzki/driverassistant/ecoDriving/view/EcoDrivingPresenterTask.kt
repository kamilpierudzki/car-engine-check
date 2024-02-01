package com.google.kpierudzki.driverassistant.ecoDriving.view

import com.google.kpierudzki.driverassistant.ecoDriving.EcoDrivingContract

class EcoDrivingPresenterTask(
        val presenter: EcoDrivingContract.Presenter,
        val mainChartLimit: Int,
        val scoreChartLimit: Int,
        val speedChartLimit: Int) {

    private var canInvoke = true

    fun invoke() {
        if (canInvoke) {
            canInvoke = false
            presenter.provideLastNSamplesForParam(
                    mainChartLimit,
                    EcoDrivingContract.EcoDrivingParameter.ACCELERATION)

            presenter.provideLastNSamplesForParam(
                    scoreChartLimit,
                    EcoDrivingContract.EcoDrivingParameter.SCORE)

            presenter.provideLastNSamplesForParam(
                    speedChartLimit,
                    EcoDrivingContract.EcoDrivingParameter.SPEED)
        }
    }
}