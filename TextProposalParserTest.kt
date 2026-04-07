package cl.javier.agendaclara.domain

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import cl.javier.agendaclara.R
import cl.javier.agendaclara.receivers.ReminderActionReceiver

class NotificationHelper(private val context: Context) {

    fun createChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        val soft = NotificationChannel(
            CHANNEL_SOFT,
            context.getString(R.string.notif_channel_soft_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.notif_channel_soft_desc)
            enableVibration(true)
        }
        val exact = NotificationChannel(
            CHANNEL_EXACT,
            context.getString(R.string.notif_channel_exact_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.notif_channel_exact_desc)
            enableVibration(true)
        }
        manager.createNotificationChannel(soft)
        manager.createNotificationChannel(exact)
    }

    fun showReminderNotification(
        notificationId: Int,
        title: String,
        body: String,
        itemId: Long,
        occurrenceKey: String,
        isExact: Boolean,
    ) {
        val builder = NotificationCompat.Builder(context, if (isExact) CHANNEL_EXACT else CHANNEL_SOFT)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)

        builder.addAction(
            0,
            "Visto",
            actionPendingIntent(itemId, occurrenceKey, notificationId, ReminderActionReceiver.ACTION_SEEN),
        )
        builder.addAction(
            0,
            "Realizado",
            actionPendingIntent(itemId, occurrenceKey, notificationId, ReminderActionReceiver.ACTION_DONE),
        )
        if (isExact) {
            builder.addAction(
                0,
                "Posponer",
                actionPendingIntent(itemId, occurrenceKey, notificationId, ReminderActionReceiver.ACTION_SNOOZE),
            )
        }

        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    }

    fun cancel(notificationId: Int) {
        NotificationManagerCompat.from(context).cancel(notificationId)
    }

    private fun actionPendingIntent(
        itemId: Long,
        occurrenceKey: String,
        notificationId: Int,
        action: String,
    ): PendingIntent {
        val intent = Intent(context, ReminderActionReceiver::class.java).apply {
            this.action = action
            putExtra(ReminderActionReceiver.EXTRA_ITEM_ID, itemId)
            putExtra(ReminderActionReceiver.EXTRA_OCCURRENCE_KEY, occurrenceKey)
            putExtra(ReminderActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
        }
        return PendingIntent.getBroadcast(
            context,
            (itemId.toInt() * 31) + action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        const val CHANNEL_SOFT = "agenda_soft"
        const val CHANNEL_EXACT = "agenda_exact"
    }
}
