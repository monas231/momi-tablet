package kr.co.jness.momi.eclean.extension

import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.widget.Toast
import androidx.annotation.ColorRes
import com.jakewharton.rxbinding4.view.clicks
import com.trello.rxlifecycle4.kotlin.bindToLifecycle
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.Consumer
import java.math.BigInteger
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

fun Context.getColorByVersion(@ColorRes mId: Int) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
    resources.getColor(mId, null)
} else {
    resources.getColor(mId)
}

/**
 * byte array 를 MD5로 변환한다.
 */
fun ByteArray.toMD5(): String {
    val md = MessageDigest.getInstance("MD5")
    return BigInteger(1, md.digest(this)).toString(16).padStart(32, '0')
}

fun View.debounceClick(callback: (Unit) -> Unit): Disposable =
    clicks().debounce(500, TimeUnit.MILLISECONDS)
        .bindToLifecycle(this)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(callback)

fun Context.showLongToast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_LONG).show()

fun Context.showToast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

val Int.dpToPx: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()