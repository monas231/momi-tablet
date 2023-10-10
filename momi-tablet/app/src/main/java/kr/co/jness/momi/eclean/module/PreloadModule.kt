package kr.co.jness.momi.eclean.module

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kr.co.jness.momi.eclean.api.ApiRepository
import kr.co.jness.momi.eclean.database.DbRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PreloadModule {

    @Singleton
    @Provides
    fun provideFilterWordScoreFilter(@ApplicationContext appContext: Context, dbRepository: DbRepository, apiRepository: ApiRepository) = FilterWordScoreFilter(appContext, dbRepository, apiRepository)

    @Singleton
    @Provides
    fun provideFilterWhitelist(dbRepository: DbRepository) = FilterWhitelist(dbRepository)


    @Singleton
    @Provides
    fun provideBrowserConfig() = BrowserConfig()

}