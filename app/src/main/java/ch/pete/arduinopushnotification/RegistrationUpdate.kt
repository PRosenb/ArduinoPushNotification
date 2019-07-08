package ch.pete.arduinopushnotification


import android.content.Context
import android.preference.PreferenceManager
import androidx.lifecycle.LiveData
import androidx.work.*
import ch.pete.arduinopushnotification.api.data.RegistrationRequest
import timber.log.Timber

class RegistrationUpdate(appContext: Context, workerParams: WorkerParameters) :
    Registration(appContext, workerParams) {

    companion object {
        const val ARG_TOKEN = "token"

        fun enqueue(token: String): LiveData<WorkInfo> {
            val dataBuilder = Data.Builder()
                .putString(ARG_TOKEN, token)
            val registrationWorkRequest =
                OneTimeWorkRequestBuilder<RegistrationUpdate>()
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

        val installationId = prefs.getString(PREF_INSTALLATION_ID, null)
        val registrationResponse =
            serverApi
                .updateRegistration(installationId, RegistrationRequest(registrationToken))
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