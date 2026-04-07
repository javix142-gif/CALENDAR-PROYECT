package cl.javier.agendaclara.domain

import cl.javier.agendaclara.data.model.ItemType
import cl.javier.agendaclara.data.model.ParsedProposal
import cl.javier.agendaclara.data.model.RepeatRule
import cl.javier.agendaclara.data.model.ScheduleMode
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.Month
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class TextProposalParser(
    private val zoneId: ZoneId = ZoneId.of("America/Santiago"),
) {
    private val monthMap = mapOf(
        "enero" to Month.JANUARY,
        "febrero" to Month.FEBRUARY,
        "marzo" to Month.MARCH,
        "abril" to Month.APRIL,
        "mayo" to Month.MAY,
        "junio" to Month.JUNE,
        "julio" to Month.JULY,
        "agosto" to Month.AUGUST,
        "septiembre" to Month.SEPTEMBER,
        "setiembre" to Month.SEPTEMBER,
        "octubre" to Month.OCTOBER,
        "noviembre" to Month.NOVEMBER,
        "diciembre" to Month.DECEMBER,
    )
    private val weekDayMap = mapOf(
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

    fun parse(
        rawInput: String,
        settingsDurationMinutes: Int = 60,
        reminderOneMinutesBefore: Int = 30,
        reminderTwoMinutesBefore: Int = 10,
        suggestedHourMinutes: Int = 9 * 60,
        now: ZonedDateTime = ZonedDateTime.now(zoneId),
    ): ParsedProposal {
        val normalized = rawInput.lowercase(Locale("es", "CL")).trim()
        val detected = mutableListOf<String>()
        val startDate = parseDate(normalized, now, detected) ?: now.toLocalDate()
        val parsedTime = parseTime(normalized, detected)
        val repeatRule = parseRepeatRule(normalized, detected)
        val location = parseLocation(rawInput, detected)
        val type = inferType(normalized)

        val (scheduleMode, finalTime, missingTime) = when {
            parsedTime != null -> Triple(ScheduleMode.FIXED, parsedTime, false)
            normalized.contains("en la mañana") || normalized.contains("en la manana") -> {
                detected += "franja mañana"
                Triple(ScheduleMode.FIXED, LocalTime.of(8, 0), false)
            }
            normalized.contains("en la tarde") -> {
                detected += "franja tarde"
                Triple(ScheduleMode.FIXED, LocalTime.of(17, 0), false)
            }
            normalized.contains("en la noche") -> {
                detected += "franja noche"
                Triple(ScheduleMode.FIXED, LocalTime.of(20, 0), false)
            }
            else -> Triple(
                ScheduleMode.FLEXIBLE_DAY,
                LocalTime.of(suggestedHourMinutes / 60, suggestedHourMinutes % 60),
                true,
            )
        }

        val title = buildTitle(rawInput, location, detected)
        val duration = parseDuration(normalized) ?: settingsDurationMinutes
        val endTime = if (scheduleMode == ScheduleMode.FIXED && finalTime != null) {
            finalTime.plusMinutes(duration.toLong())
        } else null

        return ParsedProposal(
            rawInput = rawInput,
            title = title,
            notes = if (location == null) extractResidualNotes(rawInput, title) else null,
            location = location,
            type = type,
            startDate = startDate,
            startTime = finalTime,
            endDate = startDate,
            endTime = endTime,
            durationMinutes = duration,
            repeatRule = repeatRule,
            scheduleMode = scheduleMode,
            reminderOneMinutesBefore = reminderOneMinutesBefore,
            reminderTwoMinutesBefore = reminderTwoMinutesBefore,
            needsConfirmation = true,
            missingTime = missingTime,
            detectedFragments = detected,
        )
    }

    private fun parseDate(
        normalized: String,
        now: ZonedDateTime,
        detected: MutableList<String>,
    ): LocalDate? {
        val today = now.toLocalDate()
        when {
            normalized.contains("pasado mañana") || normalized.contains("pasado manana") -> {
                detected += "pasado mañana"
                return today.plusDays(2)
            }
            normalized.contains("mañana") || normalized.contains("manana") -> {
                detected += "mañana"
                return today.plusDays(1)
            }
            normalized.contains("hoy") -> {
                detected += "hoy"
                return today
            }
        }

        val explicit = Regex("(\d{1,2})\s+de\s+([a-záéíóú]+)(?:\s+de\s+(\d{4}))?")
            .find(normalized)
        if (explicit != null) {
            val day = explicit.groupValues[1].toInt()
            val month = monthMap[explicit.groupValues[2]]
            val year = explicit.groupValues[3].takeIf { it.isNotBlank() }?.toInt() ?: today.year
            if (month != null) {
                detected += explicit.value
                var date = LocalDate.of(year, month, day)
                if (date.isBefore(today)) date = date.plusYears(1)
                return date
            }
        }

        val slash = Regex("\b(\d{1,2})[/-](\d{1,2})(?:[/-](\d{2,4}))?\b").find(normalized)
        if (slash != null) {
            val day = slash.groupValues[1].toInt()
            val month = slash.groupValues[2].toInt()
            val yearRaw = slash.groupValues[3]
            val year = when {
                yearRaw.isBlank() -> today.year
                yearRaw.length == 2 -> 2000 + yearRaw.toInt()
                else -> yearRaw.toInt()
            }
            detected += slash.value
            var date = LocalDate.of(year, month, day)
            if (date.isBefore(today)) date = date.plusYears(1)
            return date
        }

        weekDayMap.entries.firstOrNull { normalized.contains(it.key) }?.let { entry ->
            detected += entry.key
            var date = today
            var offset = (entry.value.value - today.dayOfWeek.value + 7) % 7
            if (offset == 0) offset = 7
            date = date.plusDays(offset.toLong())
            return date
        }

        return null
    }

    private fun parseTime(normalized: String, detected: MutableList<String>): LocalTime? {
        val regex = Regex("(?:a las|a la|desde las|desde la)?\s*(\d{1,2})(?::(\d{2}))?\s*(am|pm)?")
        val matches = regex.findAll(normalized).toList()
        val timeMatch = matches.firstOrNull {
            val hour = it.groupValues[1].toIntOrNull() ?: return@firstOrNull false
            hour in 0..23
        } ?: return null
        var hour = timeMatch.groupValues[1].toInt()
        val minute = timeMatch.groupValues[2].ifBlank { "0" }.toInt()
        val suffix = timeMatch.groupValues[3]
        if (suffix == "pm" && hour < 12) hour += 12
        if (suffix == "am" && hour == 12) hour = 0
        detected += timeMatch.value.trim()
        return runCatching { LocalTime.of(hour, minute) }.getOrNull()
    }

    private fun parseDuration(normalized: String): Int? {
        Regex("por\s+(\d{1,2})\s+hora").find(normalized)?.let {
            return it.groupValues[1].toInt() * 60
        }
        Regex("por\s+(\d{1,3})\s+min").find(normalized)?.let {
            return it.groupValues[1].toInt()
        }
        return null
    }

    private fun parseRepeatRule(normalized: String, detected: MutableList<String>): RepeatRule {
        return when {
            normalized.contains("todos los días") || normalized.contains("todos los dias") || normalized.contains("diariamente") -> {
                detected += "repetición diaria"
                RepeatRule.DAILY
            }
            normalized.contains("semanal") || normalized.contains("cada semana") -> {
                detected += "repetición semanal"
                RepeatRule.WEEKLY
            }
            normalized.contains("mensual") || normalized.contains("cada mes") -> {
                detected += "repetición mensual"
                RepeatRule.MONTHLY
            }
            normalized.contains("trimestral") -> {
                detected += "repetición trimestral"
                RepeatRule.QUARTERLY
            }
            normalized.contains("semestral") -> {
                detected += "repetición semestral"
                RepeatRule.SEMIANNUAL
            }
            normalized.contains("días hábiles") || normalized.contains("dias habiles") || normalized.contains("laborales") -> {
                detected += "repetición días hábiles"
                RepeatRule.WEEKDAYS
            }
            normalized.contains("anual") || normalized.contains("cada año") || normalized.contains("cada ano") -> {
                detected += "repetición anual"
                RepeatRule.YEARLY
            }
            else -> RepeatRule.NONE
        }
    }

    private fun parseLocation(raw: String, detected: MutableList<String>): String? {
        val locationRegex = Regex("\ben\s+([^,.]+)", RegexOption.IGNORE_CASE)
        val matches = locationRegex.findAll(raw).toList()
        val value = matches
            .map { it.groupValues[1].trim() }
            .lastOrNull { candidate ->
                candidate.isNotBlank() &&
                    !candidate.startsWith("la mañana", ignoreCase = true) &&
                    !candidate.startsWith("la manana", ignoreCase = true) &&
                    !candidate.startsWith("la tarde", ignoreCase = true) &&
                    !candidate.startsWith("la noche", ignoreCase = true)
            } ?: return null
        detected += "ubicación"
        return value.replaceFirstChar { it.uppercase() }
    }

    private fun inferType(normalized: String): ItemType {
        return when {
            listOf("reunión", "reunion", "cita", "cumpleaños", "cumpleanos", "control", "evento").any(normalized::contains) -> ItemType.EVENT
            listOf("pagar", "comprar", "llamar", "enviar", "revisar", "hacer", "tomar", "buscar").any(normalized::contains) -> ItemType.TASK
            else -> ItemType.REMINDER
        }
    }

    private fun buildTitle(raw: String, location: String?, detected: List<String>): String {
        var candidate = raw.trim()
        candidate = candidate.replace(Regex("^(recu[eé]rdame|recuerdame|recordarme|anota|agenda)\s+(que\s+)?", RegexOption.IGNORE_CASE), "")
        location?.let {
            candidate = candidate.replace(Regex("\ben\s+${Regex.escape(it)}", RegexOption.IGNORE_CASE), "")
        }
        detected.forEach { fragment ->
            candidate = candidate.replace(fragment, "", ignoreCase = true)
        }
        candidate = candidate.replace(Regex("\s+"), " ").trim(' ', ',', '.', ':', ';')
        if (candidate.isBlank()) return raw.trim().replaceFirstChar { it.uppercase() }
        val clean = candidate.replace(Regex("^(que\s+tengo|tengo|que|debo)\s+", RegexOption.IGNORE_CASE), "")
        return clean.replaceFirstChar { it.uppercase() }
    }

    private fun extractResidualNotes(raw: String, title: String): String? {
        val residual = raw.removePrefix(title).trim()
        return residual.takeIf { it.length >= 8 && it != raw }
    }
}
