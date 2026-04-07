package cl.javier.agendaclara.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import cl.javier.agendaclara.AgendaClaraApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        val app = context.applicationContext as AgendaClaraApp
        CoroutineScope(Dispatchers.IO).launch {
            val id = intent.getLongExtra(EXTRA_ITEM_ID, -1)
            val type = intent.getStringExtra(EXTRA_ALARM_TYPE).orEmpty()
            val item = app.container.repository.getById(id)
            if (item != null) {
                app.container.notifications.showReminder(
                    notificationId = (id * 100 + intent.getIntExtra(EXTRA_ORDINAL, 0)).toInt(),
                    title = item.title,
                    body = if (type == TYPE_EXACT) "Es hora de: ${item.title}" else "Recordatorio: ${item.title}",
                    itemId = id,
                    isExact = type == TYPE_EXACT,
                )
            }
            pending.finish()
        }
    }

    companion object {
        const val ACTION_TRIGGER = "cl.javier.agendaclara.ACTION_TRIGGER"
        const val EXTRA_ITEM_ID = "extra_item_id"
        const val EXTRA_ALARM_TYPE = "extra_alarm_type"
        const val EXTRA_ORDINAL = "extra_ordinal"
        const val TYPE_SOFT = "SOFT"
        const val TYPE_EXACT = "EXACT"
        const val TYPE_FLEX = "FLEX"
    }
}
