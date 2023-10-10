package kr.co.jness.momi.eclean.api

import io.reactivex.rxjava3.schedulers.Schedulers
import kr.co.jness.momi.eclean.BuildConfig
import kr.co.jness.momi.eclean.utils.Logger
import okhttp3.ConnectionSpec
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ServiceFactory {
    const val LOG_MAX_SIZE_PER = 1024*1024

    fun getApi(url: String): EcleanApi = getDefaultOkHttpBuilder(url)
            .run {
                val client = build()
                getDefaultRetrofitBuilder(url)
                        .client(client)
                        .build()
                        .create(EcleanApi::class.java)
            }

    private fun getDefaultOkHttpBuilder(url: String) = UnsafeOkHttpClient.getUnsafeOkHttpClient()
        .connectTimeout(1, TimeUnit.MINUTES)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .apply {
            if (url.startsWith("https")) {
                // Make http client supports All TLS and Cipher suites
                val spec = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                    .allEnabledTlsVersions()
                    .allEnabledCipherSuites()
                    .supportsTlsExtensions(true)
                    .build()
                connectionSpecs(listOf(spec))
            }
            if (Logger.SHOWLOG) {
                addInterceptor(LoggingInterceptor())
                addInterceptor(getLoggingCallbackInterceptor())
            }
        }

    private fun getDefaultRetrofitBuilder(url: String) = Retrofit.Builder()
        .baseUrl(url)
        .addCallAdapterFactory(RxJava3CallAdapterFactory.createWithScheduler(Schedulers.io()))
        .addConverterFactory(GsonConverterFactory.create())

    private fun getLoggingCallbackInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY

        }
    }
}