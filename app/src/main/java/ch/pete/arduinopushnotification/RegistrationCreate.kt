package ch.pete.arduinopushnotification

import android.content.Context
import android.preference.PreferenceManager
import androidx.lifecycle.LiveData
import androidx.work.*
import ch.pete.arduinopushnotification.api.data.NewRegistrationRequest
import timber.log.Timber

class RegistrationCreate(appContext: Context, workerParams: WorkerParameters) :
    Registration(appContext, workerParams) {

    companion object {
        private const val ARG_TOKEN = "token"

        fun enqueue(token: String, context: Context): LiveData<WorkInfo> {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            prefs.edit().putBoolean(PREF_REGISTER, true).apply()

            val dataBuilder = Data.Builder()
                .putString(ARG_TOKEN, token)
            val registrationWorkRequest =
                OneTimeWorkRequestBuilder<RegistrationCreate>()
                    .setInputData(dataBuilder.build())
                    .build()
            WorkManager.getInstance().enqueue(registrationWorkRequest)
            return WorkManager.getInstance().getWorkInfoByIdLiveData(registrationWorkRequest.id)
        }
    }

    override fun doWork(): Result {
        val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        if (!prefs.getBoolean(PREF_REGISTER, false)) {
            // not registered, nothing to do
            return Result.success();
        }

        val serverApi = createApi()
        val registrationToken = inputData.getString(ARG_TOKEN) ?: return Result.failure()

        val registrationResponse = serverApi
            .createRegistration(NewRegistrationRequest(registrationToken))
            .execute()

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
}
