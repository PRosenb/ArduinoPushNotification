package ch.pete.arduinopushnotification.api

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.ResponseBody
import okio.Buffer
import timber.log.Timber

import java.io.IOException
import java.net.URLDecoder

/**
 * Prints request and response URL and data to console.
 * Created by peterrosenberg on 14/06/2017.
 */
class LoggingInterceptor : Interceptor {
    companion object {
        private val LOG_REQUEST = true
        private val LOG_RESPONSE = true
        private val LOG_RESPONSE_HEADERS = false

        fun bodyToString(request: Request): String {
            return try {
                val copy = request.newBuilder().build()
                val buffer = Buffer()
                copy.body()?.writeTo(buffer)
                buffer.readUtf8()
            } catch (e: IOException) {
                "Could not convert body to string"
            }

        }
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val request = chain.request()
        val t1 = System.currentTimeMillis()
        if (LOG_REQUEST) {
            val requestLog = StringBuilder()
            requestLog.append("Sending ${request.method()} request\n")
            requestLog.append(request.url())
            if (chain.connection() != null) {
                requestLog.append("\n on ")
                requestLog.append(chain.connection())
            }
            requestLog.append(request.headers())

            requestLog.append("\n")
            requestLog.append(bodyToString(request))
            Timber.d(requestLog.toString())
        }
        val response = chain.proceed(request)
        val t2 = System.currentTimeMillis()
        val bodyString = response.body()?.string()

        if (LOG_RESPONSE) {
            val responseLog = StringBuilder()
            responseLog.append("Received response for\n")
            responseLog.append(URLDecoder.decode(response.request().url().toString()))
            responseLog.append("\nin ")
            responseLog.append(t2 - t1)
            responseLog.append("ms")
            if (LOG_RESPONSE_HEADERS) {
                responseLog.append(response.headers())
            }
            responseLog.append("\n")
            responseLog.append(bodyString)

            Timber.d(responseLog.toString())
        }

        return response.newBuilder()
            .body(ResponseBody.create(response.body()?.contentType(), bodyString))
            .build()
    }
}
