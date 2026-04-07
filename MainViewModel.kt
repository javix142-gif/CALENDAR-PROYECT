package cl.javier.agendaclara.data.model

import java.time.LocalDate
import java.time.LocalTime

enum class ItemType {
    EVENT,
    TASK,
    REMINDER,
}

enum class RepeatRule {
    NONE,
    DAILY,
    WEEKLY,
    MONTHLY,
    QUARTERLY,
    SEMIANNUAL,
    WEEKDAYS,
    YEARLY,
}

enum class ScheduleMode {
    FIXED,
    FLEXIBLE_DAY,
}

data class AgendaItem(
    val id: Long = 0L,
    val type: ItemType,
    val title: String,
    val notes: String?,
    val location: String?,
    val startDate: LocalDate,
    val startTime: LocalTime?,
    val endDate: LocalDate?,
    val endTime: LocalTime?,
    val durationMinutes: Int,
    val repeatRule: RepeatRule,
    val scheduleMode: ScheduleMode,
    val reminderOneMinutesBefore: Int,
    val reminderTwoMinutesBefore: Int,
    val completed: Boolean,
    val rawInput: String?,
    val createdAt: Long,
    val updatedAt: Long,
)

data class ParsedProposal(
    val rawInput: String,
    val title: String,
    val notes: String? = null,
    val location: String? = null,
    val type: ItemType = ItemType.REMINDER,
    val startDate: LocalDate,
    val startTime: LocalTime? = null,
    val endDate: LocalDate? = null,
    val endTime: LocalTime? = null,
    val durationMinutes: Int = 60,
    val repeatRule: RepeatRule = RepeatRule.NONE,
    val scheduleMode: ScheduleMode = ScheduleMode.FIXED,
    val reminderOneMinutesBefore: Int = 30,
    val reminderTwoMinutesBefore: Int = 10,
    val needsConfirmation: Boolean = true,
    val missingTime: Boolean = false,
    val detectedFragments: List<String> = emptyList(),
)

data class UserSettings(
    val reminderOneMinutesBefore: Int = 30,
    val reminderTwoMinutesBefore: Int = 10,
    val defaultDurationMinutes: Int = 60,
    val defaultSuggestedHour: Int = 9 * 60,
    val highContrast: Boolean = true,
    val exactAlarmAtEventTime: Boolean = true,
)

data class OccurrenceInfo(
    val occurrenceKey: String,
    val occurrenceDate: LocalDate,
    val occurrenceStartTime: LocalTime?,
    val scheduleMode: ScheduleMode,
    val rollingTimesMinutes: List<Int>,
    val exactTriggerMinutes: Int?,
)
