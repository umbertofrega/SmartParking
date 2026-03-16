package com.piattaforme.smartparking.activities.support

import android.app.AlertDialog
import android.content.Context
import android.widget.LinearLayout
import com.piattaforme.smartparking.R

class MapDialogDirector {

    private lateinit var dialog : AlertDialog
    private lateinit var dialogBuilder: AlertDialog.Builder
     fun makeDialog(layout : LinearLayout, context : Context, dialogBuilder: AlertDialog.Builder, onConfirm: () -> Unit) {
        this.dialogBuilder = dialogBuilder
        initialSetup(layout, context)
        addButtons(context, onConfirm)
        this.dialog = this.dialogBuilder.create()
    }

     fun getResult() : AlertDialog {
        return this.dialog
    }

    private fun initialSetup(layout: LinearLayout, context: Context) {
        this.dialogBuilder.setTitle(context.getString(R.string.alert_title))
            .setMessage(context.getString(R.string.alert_message))
            .setView(layout)
    }

    private fun addButtons(context: Context, onConfirm: () -> Unit ){
        this.dialogBuilder.setPositiveButton(context.getString(R.string.alert_button)) { _, _ ->

            onConfirm()

            this.dialog.dismiss()}

            .setNegativeButton(context.getString(R.string.alert_cancel)) { _, _ ->
                this.dialog.cancel()
            }
    }


}