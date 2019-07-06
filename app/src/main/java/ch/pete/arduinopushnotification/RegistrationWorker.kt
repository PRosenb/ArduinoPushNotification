package ch.pete.arduinopushnotification

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class RegistrationWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {
    companion object {
        const val ARG_TOKEN = "token"
    }

    override fun doWork(): Result {

        // Indicate whether the task finished successfully with the Result
        return Result.success()
    }
}
