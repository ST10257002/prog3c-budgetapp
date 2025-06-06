package vc.prog3c.poe.core.utils

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import vc.prog3c.poe.R

object Notifier {

    fun bottomSnackbar(view: View, message: String, context: Context) {
        val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT)
        snackbar.setBackgroundTint(ContextCompat.getColor(context, R.color.green))

        val textView = snackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        textView.setTextColor(ContextCompat.getColor(context, R.color.white))
        textView.textAlignment = View.TEXT_ALIGNMENT_CENTER

        snackbar.show()
    }
}

// example: Notifier.bottomSnackbar(binds.root, "🎉 Login successful!", this)