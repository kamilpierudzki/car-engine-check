package com.google.kpierudzki.driverassistant.common.view_components.aboutDialog

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import com.google.kpierudzki.driverassistant.R

class InfoDialog : DialogFragment() {

    companion object {
        val TITLE = "about_dialog_title"
        val DESCRIPTION = "about_dialog_description"
        val TAG = "InfoDialog_TAG"

        fun newInstance(title: String, description: String): InfoDialog {
            val args = Bundle()
            args.putString(TITLE, title)
            args.putString(DESCRIPTION, description)
            val dialog = InfoDialog()
            dialog.arguments = args
            return dialog
        }
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val title = arguments!!.getString(TITLE)
        val description = arguments!!.getString(DESCRIPTION)

        return AlertDialog.Builder(activity!!)
                .setTitle(title)
                .setMessage(description)
                .setPositiveButton(R.string.AboutDialog_OK, { dialog, _ ->
                    dialog.cancel()
                })
                .create()
    }
}
