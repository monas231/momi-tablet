package kr.co.jness.momi.eclean.common

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatDialog
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.DialogFragment
import kr.co.jness.momi.eclean.R

open class BaseDialogFragment: DialogFragment() {

    var progressDialog: AppCompatDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        progressDialog = AppCompatDialog(requireContext(), R.style.ThemeOverlay_AppCompat)
        progressDialog!!.setCancelable(false)
        progressDialog!!.setContentView(R.layout.dialog_progress)
        progressDialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    open fun showProgress() {
        progressDialog?.let {
            if (!it.isShowing) {
                it.show()
            }
        }
    }

    open fun hideProgress() {
        progressDialog?.let {
            if (it.isShowing) {
                it.dismiss()
            }
        }
    }

//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        return object: Dialog(requireContext()) {
//            override fun dispatchTouchEvent(event: MotionEvent): Boolean {
//                if (event!!.action === MotionEvent.ACTION_UP) {
//                    val v: View? = currentFocus
//                    if (v is EditText) {
//                        val outRect = Rect()
//                        v.getGlobalVisibleRect(outRect)
//                        if (!outRect.contains(event!!.rawX.toInt(), event!!.rawY.toInt())) {
//                            v.clearFocus()
//                            val imm =
//                                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//                            imm.hideSoftInputFromWindow(v.getWindowToken(), 0)
//                        }
//                    }
//                }
//                return super.dispatchTouchEvent(event)
//            }
//        }
//    }

}