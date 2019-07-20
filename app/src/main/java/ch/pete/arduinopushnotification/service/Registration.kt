package ch.pete.arduinopushnotification.service

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import ch.pete.arduinopushnotification.BuildConfig
import ch.pete.arduinopushnotification.api.LoggingInterceptor
import ch.pete.arduinopushnotification.api.ServerApi
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

abstract class Registration(appContext: Context, workerParams: WorkerParameters) :
        Worker(appContext, workerParams) {
    companion object {
        const val ERROR_MESSAGE_RES_ID = "errorMessageResId"
        // SERVER_URL with trailing / and until after the API version (e.g. v1)
        const val SERVER_URL = "https://pdymdkbjtg.execute-api.us-east-1.amazonaws.com/v1/"
        const val PREF_REGISTER = "register"
        const val PREF_INSTALLATION_ID = "installationId"
    }

    protected fun createApi(): ServerApi {
        val builder = Retrofit.Builder()
        builder.baseUrl(SERVER_URL)

        val gson = GsonBuilder().create()
        builder.addConverterFactory(GsonConverterFactory.create(gson))

        val client = OkHttpClient.Builder()
        if (BuildConfig.DEBUG) {
            client.interceptors().add(LoggingInterceptor())
        }
        builder.client(client.build())

        val retrofit = builder.build()

        return retrofit.create(ServerApi::class.java)
    }
}
