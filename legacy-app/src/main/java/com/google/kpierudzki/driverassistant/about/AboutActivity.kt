package com.google.kpierudzki.driverassistant.about

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import com.google.kpierudzki.driverassistant.FragmentsCallbacks
import com.google.kpierudzki.driverassistant.IMenuCreate
import com.google.kpierudzki.driverassistant.R
import com.google.kpierudzki.driverassistant.about.eco_driving.EcoDrivingProviderInfoFragment

import kotlinx.android.synthetic.main.activity_about.*
import kotlinx.android.synthetic.main.content_about.*

class AboutActivity : AppCompatActivity(), FragmentsCallbacks {

    companion object {
        const val TYPE_KEY = "type_key"
    }

    var menuCreateObservable: IMenuCreate? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        setSupportActionBar(toolbar)

        if (savedInstanceState == null) {
            intent?.let {
                val aboutType: AboutType? = it.extras.getSerializable(TYPE_KEY) as? AboutType
                aboutType?.let { type ->
                    when (type) {
                        AboutType.EcoDrivingAbout -> {
                            //todo
                        }

                        AboutType.EcoDrivingProvider -> {
                            supportFragmentManager
                                    .beginTransaction()
                                    .replace(
                                            getRootContainer().id,
                                            EcoDrivingProviderInfoFragment.newInstance())
                                    .commit()
                        }
                    }
                }
            }
        }
    }

    override fun getRootContainer(): ViewGroup {
        return aboutContainer
    }

    override fun setUseToolbarNavigationCustomAction(enabled: Boolean) {
        when (enabled) {
            true -> {
                toolbar.setNavigationOnClickListener({
                    menuCreateObservable?.onNavigationBack(false)
                })
            }

            false -> {
                toolbar.setNavigationOnClickListener({
                    onBackPressed()
                })
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return menuCreateObservable?.onMenuCreated(menuInflater, menu)
                ?: super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return menuCreateObservable?.onOptionSelected(item)
                ?: super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (menuCreateObservable == null || !menuCreateObservable!!.onNavigationBack(true)) {
            super.onBackPressed()
        }
    }
}
