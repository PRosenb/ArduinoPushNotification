package ch.pete.arduinopushnotification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import timber.log.Timber

class MessagingService : FirebaseMessagingService() {
    companion object {
        const val PREF_INSTALLATION_ID = "installationId"
        private const val CHANNEL_ID_DEFAULT = "default"
    }

    override fun onCreate() {
        super.onCreate()

        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Timber.w("getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }

                val token = task.result?.token
                sendToServer(token)
            })
    }

    override fun onNewToken(token: String?) {
        super.onNewToken(token)
        sendToServer(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        super.onMessageReceived(remoteMessage)

        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        val builder = NotificationCompat.Builder(this, CHANNEL_ID_DEFAULT)
        builder.setSmallIcon(R.mipmap.ic_launcher)
        remoteMessage?.notification?.title?.let { builder.setContentTitle(it) }
        remoteMessage?.notification?.body?.let { builder.setContentText(it) }
        builder.setAutoCancel(true)
        builder.setContentIntent(pendingIntent)

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID_DEFAULT,
                getString(R.string.default_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(channel)
        }
        manager.notify(0, builder.build())
    }

    private fun sendToServer(token: String?) {
        Timber.d("sendToServer: $token")
        RegistrationWorker.createOrUpdateToken(token)
    }
}
