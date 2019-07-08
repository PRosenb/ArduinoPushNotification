package ch.pete.arduinopushnotification

import android.content.Context
import android.preference.PreferenceManager
import androidx.lifecycle.LiveData
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import timber.log.Timber

class RegistrationDelete(appContext: Context, workerParams: WorkerParameters) :
    Registration(appContext, workerParams) {

    companion object {
        fun enqueue(context: Context): LiveData<WorkInfo> {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            prefs.edit().putBoolean(PREF_REGISTER, false).apply()

            val registrationWorkRequest =
                OneTimeWorkRequestBuilder<RegistrationDelete>().build()
            WorkManager.getInstance().enqueue(registrationWorkRequest)
            return WorkManager.getInstance().getWorkInfoByIdLiveData(registrationWorkRequest.id)
        }
    }

    override fun doWork(): Result {
        val serverApi = createApi()

        val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val installationId = prefs.getString(PREF_INSTALLATION_ID, null)
            ?: return Result.failure()

        val registrationResponse =
            serverApi
                .deleteRegistration(installationId)
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
