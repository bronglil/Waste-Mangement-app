package com.example.wms.utils


import android.content.Context
import android.view.Gravity
import android.widget.Toast

object ToastUtils {
    fun showToastAtTop(context: Context, message: String) {
        val toast = Toast.makeText(context, message, Toast.LENGTH_LONG)
        toast.setGravity(Gravity.TOP, 0, 200)
        toast.show()
    }
}