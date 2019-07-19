package ch.pete.arduinopushnotification

import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MessagingService : FirebaseMessagingService() {
    companion object {
        private val TAG = MessagingService::class.java.simpleName
    }

    override fun onCreate() {
        super.onCreate()

        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "getInstanceId failed", task.exception)
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
        Log.d(TAG, "sendToServer: $token")
    }
}