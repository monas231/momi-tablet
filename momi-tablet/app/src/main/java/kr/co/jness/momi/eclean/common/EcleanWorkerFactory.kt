package kr.co.jness.momi.eclean.common

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import kr.co.jness.momi.eclean.api.ApiRepository
import kr.co.jness.momi.eclean.database.DbRepository
import kr.co.jness.momi.eclean.database.NetworkFileUpdater
import kr.co.jness.momi.eclean.module.FilterWordScoreFilter
import javax.inject.Inject

class EcleanWorkerFactory @Inject constructor(
    private val app: EcleanApplication,
    private val apiRepo: ApiRepository,
    private val dbRepo: DbRepository,
    private val filterWord: FilterWordScoreFilter,
    private var networkFileUpdater: NetworkFileUpdater
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker {
        return EcleanWorker(appContext, workerParameters, app, apiRepo, dbRepo, filterWord, networkFileUpdater)
    }

}