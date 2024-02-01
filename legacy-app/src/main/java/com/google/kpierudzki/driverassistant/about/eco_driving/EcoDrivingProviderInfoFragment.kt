package com.google.kpierudzki.driverassistant.about.eco_driving

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.ActionBar
import android.text.Html
import android.view.*
import android.widget.Toast
import com.google.kpierudzki.driverassistant.IMenuCreate
import com.google.kpierudzki.driverassistant.R
import com.google.kpierudzki.driverassistant.about.AboutActivity
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.eco_driving_provider_common.view.*
import kotlinx.android.synthetic.main.fragment_eco_driving_provider_info.*

class EcoDrivingProviderInfoFragment : Fragment(), IMenuCreate {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_eco_driving_provider_info, container, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        prepareAppBar(false)
        clearFindViewByIdCache()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prepareAppBar(true)

//        ecoDrivingBluetoothLink.text = Html.fromHtml(resources.getString(R.string.EcoDriving_About_Provider_Link))
        ecoDrivingBluetoothLink.setOnClickListener({
            //todo
            Toast.makeText(activity, "Todo", Toast.LENGTH_SHORT).show()
        })
//        ecoDrivingBluetoothContainer.ecoDrivingProviderInfoHeaderIcon.setImageResource(R.drawable.ic_bluetooth_w)
//        ecoDrivingBluetoothContainer.ecoDrivingProviderInfoHeaderTitle.setText(R.string.EcoDriving_About_Provider_Bluetooth)
//        ecoDrivingBluetoothContainer.ecoDrivingProviderInfoGpsDescription.setText(R.string.EcoDriving_About_Description_Bluetooth)
    }

    private fun prepareAppBar(initialize: Boolean) {
        (activity as? AboutActivity)?.let { activity ->
            activity.supportActionBar?.let { actionBar ->
                when (initialize) {
                    true -> {
                        actionBar.displayOptions =
                                ActionBar.DISPLAY_SHOW_TITLE or
                                ActionBar.DISPLAY_HOME_AS_UP
//                        actionBar.title = getString(R.string.EcoDriving_About_Provider)
                        activity.menuCreateObservable = this
                    }

                    false -> {
                        actionBar.displayOptions = 0
                        activity.menuCreateObservable = null
                    }
                }
                activity.setUseToolbarNavigationCustomAction(false)
                activity.invalidateOptionsMenu()
            }
        }
    }

    override fun onMenuCreated(menuInflater: MenuInflater?, menu: Menu?): Boolean {
        return false
    }

    override fun onOptionSelected(item: MenuItem?): Boolean {
        return false
    }

    override fun onNavigationBack(fromActivity: Boolean): Boolean {
        return false
    }

    companion object {
        fun newInstance(): EcoDrivingProviderInfoFragment {
            return EcoDrivingProviderInfoFragment()
        }
    }
}
