package ch.pete.arduinopushnotification

import android.content.Context
import android.preference.PreferenceManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import ch.pete.arduinopushnotification.api.LoggingInterceptor
import ch.pete.arduinopushnotification.api.ServerApi
import ch.pete.arduinopushnotification.api.data.DeleteRegistrationRequest
import ch.pete.arduinopushnotification.api.data.NewRegistrationRequest
import ch.pete.arduinopushnotification.api.data.UpdateRegistrationRequest
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber

class RegistrationWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {
    companion object {
        const val ARG_UPDATE_OR_DELETE = "updateOrDelete"
        const val ARG_TOKEN = "token"
        private const val PREF_INSTALLATION_ID = "installationId"
        private const val API_BASE_URL = " https://5i8ur3ii0e.execute-api.us-east-2.amazonaws.com/prod/"

        enum class UpdateOrDelete {
            CREATE_OR_UPDATE, DELETE
        }
    }

    private fun createApi(): ServerApi {
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

    override fun doWork(): Result {
        return when (inputData.getString(ARG_UPDATE_OR_DELETE)?.let { UpdateOrDelete.valueOf(it) }) {
            Companion.UpdateOrDelete.CREATE_OR_UPDATE -> createOrUpdateRegistration()
            Companion.UpdateOrDelete.DELETE -> deleteRegistration()
            else -> Result.failure()
        }
    }

    private fun createOrUpdateRegistration(): Result {
        val serverApi = createApi()
        val registrationToken = inputData.getString(ARG_TOKEN) ?: return Result.failure()

        val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val installationId = prefs.getString(PREF_INSTALLATION_ID, null)
        val registrationResponse =
            if (installationId == null) {
                serverApi
                    .createRegistration(
                        NewRegistrationRequest(
                            registrationToken
                        )
                    )
                    .execute()
            } else {
                serverApi
                    .updateRegistration(
                        UpdateRegistrationRequest(
                            installationId,
                            registrationToken
                        )
                    )
                    .execute()
            }

        return if (registrationResponse.isSuccessful) {
            val registrationResult = registrationResponse.body()
            if (registrationResult?.installationId != null) {
                prefs.edit().putString(PREF_INSTALLATION_ID, registrationResult.installationId).apply()
                Timber.d("updated installationId=${registrationResult.installationId}")
                Result.success()
            } else {
                Timber.e("error: ${registrationResult?.error}")
                Result.failure()
            }
        } else {
            Timber.e("error: ${registrationResponse?.errorBody()}")
            Result.retry()
        }
    }

    private fun deleteRegistration(): Result {
        val serverApi = createApi()

        val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val installationId = prefs.getString(PREF_INSTALLATION_ID, null) ?: return Result.failure()

        val registrationResponse =
            serverApi
                .deleteRegistration(DeleteRegistrationRequest(installationId))
                .execute()

        return if (registrationResponse.isSuccessful) {
            val registrationResult = registrationResponse.body()
            if (registrationResult?.installationId != null) {
                prefs.edit().remove(PREF_INSTALLATION_ID).apply()
                Timber.d("deleted installationId=${registrationResult.installationId}")
                Result.success()
            } else {
                Timber.e("error: ${registrationResult?.error}")
                Result.failure()
            }
        } else {
            Timber.e("error: ${registrationResponse?.errorBody()}")
            Result.retry()
        }
    }
}
