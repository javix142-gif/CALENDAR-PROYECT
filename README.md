package cl.javier.agendaclara.receivers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import cl.javier.agendaclara.AgendaClaraApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.ZonedDateTime

class ReminderActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val app = context.applicationContext as AgendaClaraApplication
        val itemId = intent.getLongExtra(EXTRA_ITEM_ID, -1L)
        val occurrenceKey = intent.getStringExtra(EXTRA_OCCURRENCE_KEY).orEmpty()
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
        val action = intent.action.orEmpty()

        CoroutineScope(Dispatchers.IO).launch {
            val item = app.container.repository.getById(itemId)
            if (notificationId >= 0) app.container.notificationHelper.cancel(notificationId)
            if (item != null) {
                when (action) {
                    ACTION_SEEN -> app.container.repository.markSeen(item.id, occurrenceKey)
                    ACTION_DONE -> {
                        app.container.repository.markCompleted(item, occurrenceKey)
                        app.container.scheduler.scheduleItem(item)
                    }
                    ACTION_SNOOZE -> scheduleSnooze(context, itemId, occurrenceKey)
                }
            }
            pendingResult.finish()
        }
    }

    private fun scheduleSnooze(context: Context, itemId: Long, occurrenceKey: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            action = ReminderAlarmReceiver.ACTION_TRIGGER
            putExtra(ReminderAlarmReceiver.EXTRA_ITEM_ID, itemId)
            putExtra(ReminderAlarmReceiver.EXTRA_OCCURRENCE_KEY, occurrenceKey)
            putExtra(ReminderAlarmReceiver.EXTRA_ALARM_TYPE, ReminderAlarmReceiver.TYPE_EXACT)
            putExtra(ReminderAlarmReceiver.EXTRA_ORDINAL, 99)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (itemId.toInt() * 1000) + 99,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val triggerAt = ZonedDateTime.now(ZoneId.of("America/Santiago")).plusMinutes(10).toInstant().toEpochMilli()
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
    }

    companion object {
        const val ACTION_SEEN = "cl.javier.agendaclara.ACTION_SEEN"
        const val ACTION_DONE = "cl.javier.agendaclara.ACTION_DONE"
        const val ACTION_SNOOZE = "cl.javier.agendaclara.ACTION_SNOOZE"
        const val EXTRA_ITEM_ID = "extra_item_id"
        const val EXTRA_OCCURRENCE_KEY = "extra_occurrence_key"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    }
}
