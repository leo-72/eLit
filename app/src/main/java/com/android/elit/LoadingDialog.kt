package com.android.elit

import android.app.Dialog
import android.content.Context
import androidx.core.content.ContextCompat
import com.github.ybq.android.spinkit.SpinKitView
import com.github.ybq.android.spinkit.style.Wave

class LoadingDialog(private val context: Context) {

    private var dialog: Dialog?= null

    fun show(){
        dialog = Dialog(context)
        dialog?.setContentView(R.layout.activity_loading)

        val spinKitView = dialog?.findViewById<SpinKitView>(R.id.spin_kit)
        val wave = Wave()
        wave.color = ContextCompat.getColor(context, R.color.main_color)
        spinKitView?.setIndeterminateDrawable(wave)

        dialog?.setCancelable(false)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog?.show()

    }

    fun dismiss(){
        dialog?.dismiss()
    }
}