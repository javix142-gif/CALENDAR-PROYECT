package cl.javier.agendaclara.data.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

enum class ItemType { EVENTO, TAREA, RECORDATORIO }
enum class RepeatRule { NONE, DIARIA, SEMANAL, MENSUAL, TRIMESTRAL, SEMESTRAL, DIAS_HABILES, ANUAL }

data class AgendaItem(
    val id: Long = 0,
    val type: ItemType = ItemType.EVENTO,
    val title: String,
    val notes: String? = null,
    val location: String? = null,
    val startDate: LocalDate,
    val startTime: LocalTime? = null,
    val endDate: LocalDate? = null,
    val endTime: LocalTime? = null,
    val durationMinutes: Int = 60,
    val allDay: Boolean = false,
    val repeatRule: RepeatRule = RepeatRule.NONE,
    val reminderOneMinutesBefore: Int = 30,
    val reminderTwoMinutesBefore: Int = 10,
    val completed: Boolean = false,
    val hasExactTime: Boolean = true,
    val rawInput: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)

data class ParsedProposal(
    val title: String,
    val type: ItemType,
    val startDate: LocalDate,
    val suggestedTime: LocalTime,
    val hasExactTime: Boolean,
    val location: String? = null,
    val notes: String? = null,
    val repeatRule: RepeatRule = RepeatRule.NONE,
)

data class AppSettings(
    val reminder1: Int = 30,
    val reminder2: Int = 10,
    val defaultDurationMinutes: Int = 60,
    val highContrast: Boolean = false,
    val largeText: Boolean = true,
    val exactAlarmAtEventTime: Boolean = true,
)
