package kr.co.jness.momi.eclean.service

import android.os.Build
import android.os.FileObserver
import androidx.annotation.RequiresApi
import kr.co.jness.momi.eclean.utils.Logger
import java.io.File
import java.util.*

/**
 * 파일 감시 observer
 */
class RecursiveFileObserver(
    private val parentFile: File,
    private val mMask: Int
) {

    private val mObservers = mutableListOf<SingleFileObserver>()
    var listener: FileObserverListener? = null

    /**
     * folder loop돌면서 전체 파일을 observer에 등록 한다.
     */
    fun startWatching() {
        val stack = Stack<String>()
        stack.push(parentFile.absolutePath)

        while (!stack.empty()) {
            val parent = stack.pop()
            val path = File(parent)
            if (path.isDirectory) {
                mObservers.add(createSingleFileObserver(path, mMask))

                path.listFiles()
                    ?.filter { it.isDirectory }
                    ?.forEach {
                        stack.push(it.path)
                    }
            }
        }

        mObservers.forEach {
            it.startWatching()
        }
    }

    private fun createSingleFileObserver(file: File, mask: Int): SingleFileObserver {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            SingleFileObserver(file, mask)
        } else {
            SingleFileObserver(file.absolutePath, mask)
        }
    }

    fun stopWatching() {
        for (i in mObservers.indices) mObservers[i].stopWatching()
        mObservers.clear()
    }

    inner class SingleFileObserver : FileObserver {
        private val mPath: String

        constructor(path: String, mask: Int) : super(path, mask) {
            mPath = path
        }

        @RequiresApi(api = Build.VERSION_CODES.Q)
        constructor(file: File, mask: Int) : super(file, mask) {
            mPath = file.absolutePath
        }

        /**
         * mask 에 등록된 파일 이벤트 발생 시, observer를 통해 notify한다.
         * 현재는 file open으로 설정됨.
         */
        override fun onEvent(event: Int, path: String?) {
            if (path == null) return

            val filePath = mPath + File.separator + path
            val mFile = File(filePath)
            if (mFile.exists() && mFile.isFile) {
                listener?.onEventReceived(event, filePath)
            }
        }
    }

    interface FileObserverListener {
        fun onEventReceived(event: Int, path: String)
    }

}