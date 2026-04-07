package cl.javier.agendaclara.domain

import cl.javier.agendaclara.data.model.AgendaItem
import cl.javier.agendaclara.data.model.OccurrenceInfo
import cl.javier.agendaclara.data.model.RepeatRule
import cl.javier.agendaclara.data.model.ScheduleMode
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

object OccurrenceCalculator {
    private val zoneId: ZoneId = ZoneId.of("America/Santiago")

    fun nextOccurrence(item: AgendaItem, now: ZonedDateTime = ZonedDateTime.now(zoneId)): OccurrenceInfo? {
        if (item.completed) return null

        var candidateDate = item.startDate
        val time = item.startTime
        if (item.repeatRule == RepeatRule.NONE) {
            if (candidateDate.isBefore(now.toLocalDate())) return null
            if (candidateDate.isEqual(now.toLocalDate()) && time != null && time.isBefore(now.toLocalTime()) && item.scheduleMode == ScheduleMode.FIXED) {
                return null
            }
            return buildOccurrence(item, candidateDate, now)
        }

        while (true) {
            val occurrence = buildOccurrence(item, candidateDate, now)
            if (occurrence != null) return occurrence
            candidateDate = nextDate(candidateDate, item.repeatRule)
            if (candidateDate.isAfter(now.toLocalDate().plusYears(3))) return null
        }
    }

    fun buildOccurrence(item: AgendaItem, date: LocalDate, now: ZonedDateTime): OccurrenceInfo? {
        val time = item.startTime
        return when (item.scheduleMode) {
            ScheduleMode.FIXED -> {
                if (time == null) return null
                if (date.isBefore(now.toLocalDate())) return null
                if (date.isEqual(now.toLocalDate()) && time.isBefore(now.toLocalTime())) {
                    if (item.repeatRule == RepeatRule.NONE) return null
                }
                OccurrenceInfo(
                    occurrenceKey = "${item.id}|${date}|FIXED",
                    occurrenceDate = date,
                    occurrenceStartTime = time,
                    scheduleMode = ScheduleMode.FIXED,
                    rollingTimesMinutes = emptyList(),
                    exactTriggerMinutes = time.hour * 60 + time.minute,
                )
            }
            ScheduleMode.FLEXIBLE_DAY -> {
                if (date.isBefore(now.toLocalDate())) return null
                val anchor = time ?: LocalTime.of(9, 0)
                val times = mutableListOf<Int>()
                var nextMinutes = anchor.hour * 60 + anchor.minute
                val nowMinutes = now.toLocalTime().hour * 60 + now.toLocalTime().minute
                if (date.isEqual(now.toLocalDate()) && nextMinutes < nowMinutes) {
                    val delta = ((nowMinutes - nextMinutes) / 120 + 1) * 120
                    nextMinutes += delta
                }
                while (nextMinutes <= 23 * 60) {
                    times += nextMinutes
                    nextMinutes += 120
                }
                if (times.isEmpty()) return null
                OccurrenceInfo(
                    occurrenceKey = "${item.id}|${date}|FLEX",
                    occurrenceDate = date,
                    occurrenceStartTime = anchor,
                    scheduleMode = ScheduleMode.FLEXIBLE_DAY,
                    rollingTimesMinutes = times,
                    exactTriggerMinutes = null,
                )
            }
        }
    }

    fun nextDate(current: LocalDate, rule: RepeatRule): LocalDate {
        return when (rule) {
            RepeatRule.NONE -> current
            RepeatRule.DAILY -> current.plusDays(1)
            RepeatRule.WEEKLY -> current.plusWeeks(1)
            RepeatRule.MONTHLY -> current.plusMonths(1)
            RepeatRule.QUARTERLY -> current.plusMonths(3)
            RepeatRule.SEMIANNUAL -> current.plusMonths(6)
            RepeatRule.WEEKDAYS -> {
                var candidate = current.plusDays(1)
                while (candidate.dayOfWeek == DayOfWeek.SATURDAY || candidate.dayOfWeek == DayOfWeek.SUNDAY) {
                    candidate = candidate.plusDays(1)
                }
                candidate
            }
            RepeatRule.YEARLY -> current.plusYears(1)
        }
    }
}
