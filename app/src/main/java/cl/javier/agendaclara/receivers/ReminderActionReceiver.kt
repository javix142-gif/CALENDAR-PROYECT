package cl.javier.agendaclara.receivers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import cl.javier.agendaclara.AgendaClaraApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        val app = context.applicationContext as AgendaClaraApp
        CoroutineScope(Dispatchers.IO).launch {
            val id = intent.getLongExtra(EXTRA_ITEM_ID, -1)
            val notif = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
            if (notif > 0) app.container.notifications.cancel(notif)
            when (intent.action) {
                ACTION_DONE -> app.container.repository.markCompleted(id, true)
                ACTION_SNOOZE -> snooze(context, id)
            }
            pending.finish()
        }
    }

    private fun snooze(context: Context, id: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            action = ReminderAlarmReceiver.ACTION_TRIGGER
            putExtra(ReminderAlarmReceiver.EXTRA_ITEM_ID, id)
            putExtra(ReminderAlarmReceiver.EXTRA_ALARM_TYPE, ReminderAlarmReceiver.TYPE_EXACT)
            putExtra(ReminderAlarmReceiver.EXTRA_ORDINAL, 99)
        }
        val pi = PendingIntent.getBroadcast(context, (id * 1000).toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 10 * 60 * 1000, pi)
    }

    companion object {
        const val ACTION_SEEN = "cl.javier.agendaclara.ACTION_SEEN"
        const val ACTION_DONE = "cl.javier.agendaclara.ACTION_DONE"
        const val ACTION_SNOOZE = "cl.javier.agendaclara.ACTION_SNOOZE"
        const val EXTRA_ITEM_ID = "extra_item_id"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    }
}
