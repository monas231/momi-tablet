package kr.co.jness.momi.eclean.common

import android.app.Application
import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kr.co.jness.momi.eclean.api.ApiRepository
import kr.co.jness.momi.eclean.database.*
import kr.co.jness.momi.eclean.module.FilterWordScoreFilter
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SingletonModule {
    /**
     * DB Single instance
     */

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context) =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideApiCacheDao(db: AppDatabase) = db.apiCacheDao()

    @Provides
    fun provideRuleInfoDao(db: AppDatabase) = db.ruleInfoDao()

    @Provides
    fun provideLocalVideoHashDao(db: AppDatabase) = db.localVideoHashDao()

    @Provides
    fun provideVideoHashDao(db: AppDatabase) = db.videoHashDao()

    @Provides
    fun provideWhitelistDao(db: AppDatabase) = db.whitelistDao()

    @Provides
    fun provideWordFilterDao(db: AppDatabase) = db.wordFilterDao()

    @Provides
    fun provideLocalWhitelistDao(db: AppDatabase) = db.localWhitelistDao()

    @Provides
    @Singleton
    fun provideApiRepository() = ApiRepository()

    @Provides
    fun provideDbRepository(
        apiCacheDao: ApiCacheDao,
        ruleInfoDao: RuleInfoDao,
        localVideoHashDao: LocalVideoHashDao,
        videoHashDao: VideoHashDao,
        whitelistDao: WhitelistDao,
        wordFilterDao: WordFilterDao,
        localWhitelistDao: LocalWhitelistDao
    ) = DbRepository(
        apiCacheDao,
        ruleInfoDao,
        localVideoHashDao,
        videoHashDao,
        whitelistDao,
        wordFilterDao,
        localWhitelistDao
    )

    @Provides
    fun provideEcleanApplication(application: Application) = application as EcleanApplication

    @Provides
    @Singleton
    fun provideNetworkUpdater(@ApplicationContext appContext: Context, dbRepository: DbRepository, apiRepository: ApiRepository, filterWord: FilterWordScoreFilter) = NetworkFileUpdater(dbRepository, apiRepository, filterWord)
}