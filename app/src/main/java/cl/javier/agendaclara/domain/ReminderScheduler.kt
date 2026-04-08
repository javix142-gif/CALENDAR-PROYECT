package cl.javier.agendaclara.domain

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import cl.javier.agendaclara.data.model.AgendaItem
import cl.javier.agendaclara.receivers.ReminderAlarmReceiver
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class ReminderScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val zone = ZoneId.systemDefault()

    fun schedule(item: AgendaItem) {
        cancel(item.id)
        val baseTime = item.startTime ?: LocalTime.of(9, 0)
        val dateTime = LocalDateTime.of(item.startDate, baseTime)
        if (item.hasExactTime) {
            listOf(item.reminderOneMinutesBefore, item.reminderTwoMinutesBefore).forEachIndexed { index, min ->
                setAlarm(item.id, dateTime.minusMinutes(min.toLong()), ReminderAlarmReceiver.TYPE_SOFT, index + 1)
            }
            setAlarm(item.id, dateTime, ReminderAlarmReceiver.TYPE_EXACT, 3)
        } else if (item.type != cl.javier.agendaclara.data.model.ItemType.EVENTO) {
            var current = baseTime
            var index = 1
            while (current.isBefore(LocalTime.of(23, 59))) {
                setAlarm(item.id, LocalDateTime.of(item.startDate, current), ReminderAlarmReceiver.TYPE_FLEX, index++)
                current = current.plusHours(2)
            }
        }
    }

    fun cancel(itemId: Long) {
        for (i in 1..20) {
            listOf(ReminderAlarmReceiver.TYPE_SOFT, ReminderAlarmReceiver.TYPE_EXACT, ReminderAlarmReceiver.TYPE_FLEX).forEach { type ->
                alarmManager.cancel(pending(itemId, type, i, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE))
            }
        }
    }

    fun rescheduleAll(items: List<AgendaItem>) = items.forEach(::schedule)

    private fun setAlarm(itemId: Long, at: LocalDateTime, type: String, index: Int) {
        val millis = at.atZone(zone).toInstant().toEpochMilli()
        if (millis <= System.currentTimeMillis()) return
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pending(itemId, type, index, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
    }

    private fun pending(itemId: Long, type: String, index: Int, flags: Int): PendingIntent {
        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            action = ReminderAlarmReceiver.ACTION_TRIGGER
            putExtra(ReminderAlarmReceiver.EXTRA_ITEM_ID, itemId)
            putExtra(ReminderAlarmReceiver.EXTRA_ALARM_TYPE, type)
            putExtra(ReminderAlarmReceiver.EXTRA_ORDINAL, index)
        }
        return PendingIntent.getBroadcast(context, (itemId.toInt() * 100) + index + type.hashCode(), intent, flags)
    }
}
