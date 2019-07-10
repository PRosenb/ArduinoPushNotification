package ch.pete.arduinopushnotification.service

import android.content.Context
import android.preference.PreferenceManager
import androidx.lifecycle.LiveData
import androidx.work.*
import ch.pete.arduinopushnotification.R
import timber.log.Timber
import java.net.UnknownHostException

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

        try {
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
        } catch (e: UnknownHostException) {
            return Result.failure(
                    Data.Builder().putInt(ERROR_MESSAGE_RES_ID, R.string.wrong_host_error).build()
            )
        }
    }
}
