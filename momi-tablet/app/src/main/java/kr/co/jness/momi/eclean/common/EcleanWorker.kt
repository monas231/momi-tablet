package kr.co.jness.momi.eclean.common

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.co.jness.momi.eclean.api.ApiRepository
import kr.co.jness.momi.eclean.database.DbRepository
import kr.co.jness.momi.eclean.database.NetworkFileUpdater
import kr.co.jness.momi.eclean.module.FilterWordScoreFilter
import kr.co.jness.momi.eclean.module.SetupDb
import kr.co.jness.momi.eclean.utils.Logger
import java.util.*

const val FCM_TOKEN = "fcm_token"
const val WORK_TYPE = "work_type"

@HiltWorker
class EcleanWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted params: WorkerParameters,
    private val app: EcleanApplication,
    private val apiRepo: ApiRepository,
    private val dbRepo: DbRepository,
    private var filterWord: FilterWordScoreFilter,
    private var networkFileUpdater: NetworkFileUpdater
) : Worker(context, params) {

    val disposable = CompositeDisposable()

    override fun onStopped() {
        Logger.d("EcleanWorker onStopped")

        disposable.clear()
        super.onStopped()
    }

    /**
     * API 작업 수행
     */
    override fun doWork(): Result {

        Logger.d("EcleanWorker doWork = ${inputData.getInt(WORK_TYPE, 0)}")

        return when (inputData.getInt(WORK_TYPE, 0)) {
            WorkType.REGISTER_FCM_TOKEN.number -> {
                /**
                 * FCM Token 등록
                 */
                val deviceId = app.getDeviceId()
                val schoolId = app.getSchoolId()
                val token = inputData.getString(FCM_TOKEN)

                if (deviceId != null && schoolId != null && token != null) {
                    disposable += apiRepo.registerFCMToken(deviceId, schoolId, token)
                        .subscribe({}, {})
                    Result.success()
                } else {
                    Result.retry()
                }
            }
            WorkType.UPDATE_RULE_INFO.number -> {
                /**
                 * 정책정보 업데이트
                 */
                val deviceId = app.getDeviceId()
                val schoolId = app.getSchoolId()

                if (deviceId != null && schoolId != null) {
                    disposable += apiRepo.requestRuleInfo(deviceId, schoolId)
                        .subscribe({ res ->
                            CoroutineScope(Dispatchers.IO).launch {
                                val savedData = res.data
                                savedData.updatedAt = Calendar.getInstance().time
                                dbRepo.setRuleInfo(listOf(savedData))
                                // 규칙정보가 업데이트되면 history 도 비워준다.
                                dbRepo.deleteAllApiCache()
                                    .subscribe()
                            }
                        }, {})

                    disposable += apiRepo.checkFile(deviceId, schoolId)
                        .subscribeOn(Schedulers.io())
                        .subscribe(
                            { res ->
                                networkFileUpdater.addFileList(res.data.fileList)

                            }, {
                                Logger.e(it.message ?: "")
                            }
                        )

                    disposable += filterWord.updateKeywordApi(deviceId, schoolId)

                    Result.success()
                }
                else {
                    Result.failure()
                }
            }
            WorkType.INSERT_DB.number-> {

                SetupDb.insertLocalVideoHashDB(context, dbRepo)?.let {
                    disposable += it
                }
                SetupDb.insertLocalWhitelistDB(context, dbRepo)?.let {
                    disposable += it
                }

                Result.success()
            }
            else -> Result.failure()
        }
    }

}

enum class WorkType(val number: Int) {
    REGISTER_FCM_TOKEN(1), UPDATE_RULE_INFO(2), INSERT_DB(3)
}