package ch.pete.arduinopushnotification

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import ch.pete.arduinopushnotification.api.LoggingInterceptor
import ch.pete.arduinopushnotification.api.ServerApi
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

abstract class Registration(appContext: Context, workerParams: WorkerParameters) :
        Worker(appContext, workerParams) {
    companion object {
        private const val API_BASE_URL = " https://5i8ur3ii0e.execute-api.us-east-2.amazonaws.com/prod/"

        const val PREF_REGISTER = "register"
        const val PREF_INSTALLATION_ID = "installationId"
    }

    protected fun createApi(): ServerApi {
        val builder = Retrofit.Builder()
        builder.baseUrl(API_BASE_URL)

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
