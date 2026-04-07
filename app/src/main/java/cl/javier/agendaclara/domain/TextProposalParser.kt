package cl.javier.agendaclara.domain

import cl.javier.agendaclara.data.model.ItemType
import cl.javier.agendaclara.data.model.ParsedProposal
import cl.javier.agendaclara.data.model.RepeatRule
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

class TextProposalParser {
    fun parse(input: String, today: LocalDate = LocalDate.now()): ParsedProposal {
        val normalized = input.lowercase().trim()
        val date = parseDate(normalized, today)
        val (time, exact) = parseTime(normalized)
        val type = when {
            normalized.contains("recordar") || normalized.contains("recordatorio") -> ItemType.RECORDATORIO
            normalized.contains("tarea") || normalized.contains("hacer") -> ItemType.TAREA
            else -> ItemType.EVENTO
        }
        val repeat = parseRepeat(normalized)
        val location = Regex("en ([a-záéíóú0-9 ]+)").find(normalized)?.groupValues?.get(1)
        val title = input.trim().replaceFirstChar { it.uppercase() }
        return ParsedProposal(
            title = title,
            type = type,
            startDate = date,
            suggestedTime = time,
            hasExactTime = exact,
            location = location,
            notes = if (exact) null else "Hora sugerida editable",
            repeatRule = repeat,
        )
    }

    private fun parseDate(text: String, today: LocalDate): LocalDate {
        if (text.contains("mañana")) return today.plusDays(1)
        val days = mapOf(
            "lunes" to DayOfWeek.MONDAY,
            "martes" to DayOfWeek.TUESDAY,
            "miércoles" to DayOfWeek.WEDNESDAY,
            "miercoles" to DayOfWeek.WEDNESDAY,
            "jueves" to DayOfWeek.THURSDAY,
            "viernes" to DayOfWeek.FRIDAY,
            "sábado" to DayOfWeek.SATURDAY,
            "sabado" to DayOfWeek.SATURDAY,
            "domingo" to DayOfWeek.SUNDAY,
        )
        days.entries.firstOrNull { text.contains(it.key) }?.let { (_, day) ->
            var diff = (day.value - today.dayOfWeek.value + 7) % 7
            if (diff == 0) diff = 7
            return today.plusDays(diff.toLong())
        }
        return today
    }

    private fun parseTime(text: String): Pair<LocalTime, Boolean> {
        Regex("(\\d{1,2}):(\\d{2})").find(text)?.let {
            return LocalTime.of(it.groupValues[1].toInt(), it.groupValues[2].toInt()) to true
        }
        if (text.contains("mañana en la mañana")) return LocalTime.of(8, 0) to false
        if (text.contains("mañana en la tarde")) return LocalTime.of(17, 0) to false
        return LocalTime.of(9, 0) to false
    }

    private fun parseRepeat(text: String): RepeatRule = when {
        text.contains("diario") || text.contains("cada día") -> RepeatRule.DIARIA
        text.contains("semanal") || text.contains("cada semana") -> RepeatRule.SEMANAL
        text.contains("mensual") -> RepeatRule.MENSUAL
        text.contains("trimestral") -> RepeatRule.TRIMESTRAL
        text.contains("semestral") -> RepeatRule.SEMESTRAL
        text.contains("hábiles") || text.contains("habiles") -> RepeatRule.DIAS_HABILES
        text.contains("anual") -> RepeatRule.ANUAL
        else -> RepeatRule.NONE
    }
}
