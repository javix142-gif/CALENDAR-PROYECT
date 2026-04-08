package cl.javier.agendaclara.data.repository

import cl.javier.agendaclara.data.local.AgendaDao
import cl.javier.agendaclara.data.local.AgendaItemEntity
import cl.javier.agendaclara.data.model.AgendaItem
import cl.javier.agendaclara.data.model.ItemType
import cl.javier.agendaclara.data.model.RepeatRule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

class AgendaRepository(private val dao: AgendaDao) {
    fun observeToday(date: LocalDate): Flow<List<AgendaItem>> = dao.observeByDate(date.toEpochDay()).map { it.map(::toDomain) }
    fun observeUpcoming(from: LocalDate, to: LocalDate): Flow<List<AgendaItem>> = dao.observeByRange(from.toEpochDay(), to.toEpochDay()).map { it.map(::toDomain) }

    suspend fun getById(id: Long): AgendaItem? = dao.getById(id)?.let(::toDomain)

    suspend fun save(item: AgendaItem): Long {
        val entity = toEntity(item)
        return if (item.id == 0L) dao.insert(entity) else {
            dao.update(entity)
            item.id
        }
    }

    suspend fun markCompleted(id: Long, completed: Boolean = true) {
        val current = dao.getById(id) ?: return
        dao.update(current.copy(completed = completed, updatedAtEpochSec = nowSec()))
    }

    private fun toDomain(e: AgendaItemEntity) = AgendaItem(
        id = e.id,
        type = ItemType.valueOf(e.type),
        title = e.title,
        notes = e.notes,
        location = e.location,
        startDate = LocalDate.ofEpochDay(e.startDateEpochDay),
        startTime = e.startTimeMinutes?.let { LocalTime.of(it / 60, it % 60) },
        endDate = e.endDateEpochDay?.let(LocalDate::ofEpochDay),
        endTime = e.endTimeMinutes?.let { LocalTime.of(it / 60, it % 60) },
        durationMinutes = e.durationMinutes,
        allDay = e.allDay,
        repeatRule = RepeatRule.valueOf(e.repeatRule),
        reminderOneMinutesBefore = e.reminderOneMinutesBefore,
        reminderTwoMinutesBefore = e.reminderTwoMinutesBefore,
        completed = e.completed,
        hasExactTime = e.hasExactTime,
        rawInput = e.rawInput,
        createdAt = LocalDateTime.ofEpochSecond(e.createdAtEpochSec, 0, ZoneOffset.UTC),
        updatedAt = LocalDateTime.ofEpochSecond(e.updatedAtEpochSec, 0, ZoneOffset.UTC),
    )

    private fun toEntity(a: AgendaItem) = AgendaItemEntity(
        id = a.id,
        type = a.type.name,
        title = a.title,
        notes = a.notes,
        location = a.location,
        startDateEpochDay = a.startDate.toEpochDay(),
        startTimeMinutes = a.startTime?.let { it.hour * 60 + it.minute },
        endDateEpochDay = a.endDate?.toEpochDay(),
        endTimeMinutes = a.endTime?.let { it.hour * 60 + it.minute },
        durationMinutes = a.durationMinutes,
        allDay = a.allDay,
        repeatRule = a.repeatRule.name,
        reminderOneMinutesBefore = a.reminderOneMinutesBefore,
        reminderTwoMinutesBefore = a.reminderTwoMinutesBefore,
        completed = a.completed,
        hasExactTime = a.hasExactTime,
        rawInput = a.rawInput,
        createdAtEpochSec = if (a.id == 0L) nowSec() else a.createdAt.toEpochSecond(ZoneOffset.UTC),
        updatedAtEpochSec = nowSec(),
    )

    private fun nowSec() = Instant.now().epochSecond
}
