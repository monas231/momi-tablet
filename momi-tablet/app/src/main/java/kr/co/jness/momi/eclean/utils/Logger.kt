package kr.co.jness.momi.eclean.utils

import android.util.Log
import kr.co.jness.momi.eclean.BuildConfig

object Logger {

    var prefix = "Mom-i-Eclean"
    var TAG = "Eclean"
    val SHOWLOG = BuildConfig.DEBUG // BuildConfig.DEBUG


    fun d(message: String) {
        if(SHOWLOG) {
            Log.d(TAG, "${getTag()} $message")
        }
    }
    fun e(message: String) {
        if(SHOWLOG) {
            Log.e(TAG, "${getTag()} $message")
        }
    }

    fun getTag(): String {
//        getTrace()?.let {trace->
//            var className = trace.className
//            val lastIndex = className.lastIndexOf(".")
//            className = className.substring(lastIndex + 1)
//            val methodName = trace.methodName
//            val fileName = trace.fileName
//            val lineNo = trace.lineNumber
//            return if (SHOWLOG) {
//                "$prefix$className:$methodName ($fileName:$lineNo)"
//            } else {
//                "$prefix$className:$methodName"
//            }
//        }
        return prefix
    }

    private fun getTrace(): StackTraceElement? {
        val stack = Throwable().fillInStackTrace()
        val trace = stack.stackTrace
        val length = trace.size
        var index = 0
        if (length > 0) {
            for (i in 0 until length) {
                val className = trace[i].className
                if (!className.equals(
                        Logger::class.java.getName(),
                        ignoreCase = true
                    )
                ) {
                    index = i
                    break
                }
            }
        }
        return trace[index]
    }
}