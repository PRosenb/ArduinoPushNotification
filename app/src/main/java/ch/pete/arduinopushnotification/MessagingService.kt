package ch.pete.arduinopushnotification

import android.widget.Toast
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import timber.log.Timber

class MessagingService : FirebaseMessagingService() {
    companion object {
        const val PREF_INSTALLATION_ID = "installationId"
    }

    override fun onCreate() {
        super.onCreate()

        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Timber.w("getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new Instance ID token
                val token = task.result?.token
                sendToServer(token)
                Toast.makeText(baseContext, token, Toast.LENGTH_SHORT).show()
            })
    }

    override fun onNewToken(token: String?) {
        super.onNewToken(token)
        sendToServer(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        super.onMessageReceived(remoteMessage)
    }

    private fun sendToServer(token: String?) {
        Timber.d("sendToServer: $token")
        RegistrationWorker.createOrUpdateToken(token)
    }
}
