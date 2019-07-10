package ch.pete.arduinopushnotification

import android.content.Context
import android.preference.PreferenceManager
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
        const val ERROR_MESSAGE_RES_ID = "errorMessageResId"
        const val PREF_SERVER_URL = "serverUrl"
        const val PREF_REGISTER = "register"
        const val PREF_INSTALLATION_ID = "installationId"
    }

    protected fun createApi(): ServerApi {
        val builder = Retrofit.Builder()
        val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        var serverBaseUrl = prefs.getString(PREF_SERVER_URL, null)
        if (serverBaseUrl == null) {
            serverBaseUrl = applicationContext.getString(R.string.default_server_url)
                    ?: "" // cannot happen
        }
        if (!serverBaseUrl.endsWith('/')) {
            serverBaseUrl += "/"
        }
        builder.baseUrl(serverBaseUrl)

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
