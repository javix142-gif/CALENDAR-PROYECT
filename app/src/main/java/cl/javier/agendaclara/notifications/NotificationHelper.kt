package cl.javier.agendaclara.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import cl.javier.agendaclara.MainActivity
import cl.javier.agendaclara.R
import cl.javier.agendaclara.receivers.ReminderActionReceiver

class NotificationHelper(private val context: Context) {
    fun createChannels() {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(NotificationChannel(CHANNEL_SOFT, "Recordatorios", NotificationManager.IMPORTANCE_DEFAULT))
        manager.createNotificationChannel(NotificationChannel(CHANNEL_EXACT, "Alarmas exactas", NotificationManager.IMPORTANCE_HIGH))
    }

    fun showReminder(notificationId: Int, title: String, body: String, itemId: Long, isExact: Boolean) {
        val openIntent = PendingIntent.getActivity(context, notificationId, Intent(context, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val seenAction = actionIntent(ReminderActionReceiver.ACTION_SEEN, itemId, notificationId)
        val doneAction = actionIntent(ReminderActionReceiver.ACTION_DONE, itemId, notificationId)
        val builder = NotificationCompat.Builder(context, if (isExact) CHANNEL_EXACT else CHANNEL_SOFT)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(openIntent)
            .setAutoCancel(true)
            .addAction(0, "Visto", seenAction)
            .addAction(0, "Realizado", doneAction)
        if (isExact) builder.addAction(0, "Posponer", actionIntent(ReminderActionReceiver.ACTION_SNOOZE, itemId, notificationId))
        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    }

    fun cancel(notificationId: Int) = NotificationManagerCompat.from(context).cancel(notificationId)

    private fun actionIntent(action: String, itemId: Long, notificationId: Int): PendingIntent {
        val intent = Intent(context, ReminderActionReceiver::class.java).apply {
            this.action = action
            putExtra(ReminderActionReceiver.EXTRA_ITEM_ID, itemId)
            putExtra(ReminderActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
        }
        return PendingIntent.getBroadcast(context, action.hashCode() + notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    companion object {
        const val CHANNEL_SOFT = "soft_channel"
        const val CHANNEL_EXACT = "exact_channel"
    }
}
