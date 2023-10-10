package kr.co.jness.momi.eclean.api

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import okio.IOException
import java.util.*

class LoggingInterceptor : Interceptor {

    companion object {
        const val TAG = "LoggingInterceptor"
    }

    /**
     * API 통신 로그
     */
    override fun intercept(chain: Interceptor.Chain): Response {

        val builder = StringBuilder()

        val request = chain.request()
        builder.append("* Date : ${Date().toLocaleString()} *\n")
        builder.append("* Request *\n")
        builder.append("* URL : ${request.url}\n")
        builder.append("* headers\n")
        request.headers?.forEach {
            builder.append("** ${it.first} : ${it.second}\n")
        }
        request.body?.let {
            builder.append("* content-type : ${it.contentType()}\n")
        }
        bodyToString(request)?.let {
            if (!it.isNullOrEmpty()) {
                builder.append("* body : ${it}\n")
            }
        }

        var response = chain.proceed(request)

        try {
            builder.append("------------------------------------------------\n")
            builder.append("* Response *\n")
            builder.append("* code : ${response.code}\n")

            response.newBuilder().build().body?.let {
                builder.append("* content-type : ${it.contentType()}\n")

                it.contentType()?.let { type ->

                    if (type.subtype.contains("json") || type.type.contains("text")) {
                        val responseStr = it.string()
                        builder.append("* data : ${responseStr}\n")

                        val body = responseStr.toResponseBody(type)
                        response = response.newBuilder().body(body).build()
                    } else {
                        builder.append("* data format is : ${type.type}\n")
                    }
                }
            }
        } catch (e: Exception) {
            builder.append("*** Error making Network Log<br/>")
        }

        return response
    }

    private fun bodyToString(request: Request): String? {
        return try {
            val copy: Request = request.newBuilder().build()
            val buffer = Buffer()
            copy.body?.writeTo(buffer)
            buffer.readUtf8()
        } catch (e: IOException) {
            e.message
        }
    }
}