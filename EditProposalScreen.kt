package cl.javier.agendaclara.data.repository

import cl.javier.agendaclara.data.local.AgendaDao
import cl.javier.agendaclara.data.local.AgendaItemEntity
import cl.javier.agendaclara.data.local.OccurrenceStateDao
import cl.javier.agendaclara.data.local.OccurrenceStateEntity
import cl.javier.agendaclara.data.model.AgendaItem
import cl.javier.agendaclara.data.model.ItemType
import cl.javier.agendaclara.data.model.ParsedProposal
import cl.javier.agendaclara.data.model.RepeatRule
import cl.javier.agendaclara.data.model.ScheduleMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

class AgendaRepository(
    private val agendaDao: AgendaDao,
    private val occurrenceStateDao: OccurrenceStateDao,
) {
    fun observeActiveItems(): Flow<List<AgendaItem>> =
        agendaDao.observeActiveItems().map { list -> list.map { it.toDomain() } }

    suspend fun getActiveItems(): List<AgendaItem> = agendaDao.getActiveItems().map { it.toDomain() }

    suspend fun getById(id: Long): AgendaItem? = agendaDao.getById(id)?.toDomain()

    suspend fun saveProposal(proposal: ParsedProposal, existingId: Long? = null): AgendaItem {
        val now = Instant.now().toEpochMilli()
        val entity = AgendaItemEntity(
            id = existingId ?: 0L,
            type = proposal.type.name,
            title = proposal.title.trim(),
            notes = proposal.notes?.trim()?.ifBlank { null },
            location = proposal.location?.trim()?.ifBlank { null },
            startDateEpochDay = proposal.startDate.toEpochDay(),
            startTimeMinutes = proposal.startTime?.toMinuteOfDay(),
            endDateEpochDay = proposal.endDate?.toEpochDay(),
            endTimeMinutes = proposal.endTime?.toMinuteOfDay(),
            durationMinutes = proposal.durationMinutes,
            repeatRule = proposal.repeatRule.name,
            scheduleMode = proposal.scheduleMode.name,
            reminderOneMinutesBefore = proposal.reminderOneMinutesBefore,
            reminderTwoMinutesBefore = proposal.reminderTwoMinutesBefore,
            completed = false,
            rawInput = proposal.rawInput,
            createdAt = if (existingId == null) now else (agendaDao.getById(existingId)?.createdAt ?: now),
            updatedAt = now,
        )
        val id = agendaDao.insert(entity)
        return agendaDao.getById(id)?.toDomain() ?: entity.copy(id = id).toDomain()
    }

    suspend fun markCompleted(item: AgendaItem, occurrenceKey: String? = null) {
        if (item.repeatRule == RepeatRule.NONE) {
            agendaDao.markCompleted(item.id, Instant.now().toEpochMilli())
        } else if (occurrenceKey != null) {
            occurrenceStateDao.upsert(
                OccurrenceStateEntity(
                    itemId = item.id,
                    occurrenceKey = occurrenceKey,
                    status = "COMPLETED",
                    updatedAt = Instant.now().toEpochMilli(),
                )
            )
        }
    }

    suspend fun markSeen(itemId: Long, occurrenceKey: String) {
        occurrenceStateDao.upsert(
            OccurrenceStateEntity(
                itemId = itemId,
                occurrenceKey = occurrenceKey,
                status = "SEEN",
                updatedAt = Instant.now().toEpochMilli(),
            )
        )
    }

    suspend fun getOccurrenceStatus(itemId: Long, occurrenceKey: String): String? =
        occurrenceStateDao.getState(itemId, occurrenceKey)?.status

    private fun AgendaItemEntity.toDomain(): AgendaItem {
        return AgendaItem(
            id = id,
            type = ItemType.valueOf(type),
            title = title,
            notes = notes,
            location = location,
            startDate = LocalDate.ofEpochDay(startDateEpochDay),
            startTime = startTimeMinutes?.let(::minuteOfDayToLocalTime),
            endDate = endDateEpochDay?.let(LocalDate::ofEpochDay),
            endTime = endTimeMinutes?.let(::minuteOfDayToLocalTime),
            durationMinutes = durationMinutes,
            repeatRule = RepeatRule.valueOf(repeatRule),
            scheduleMode = ScheduleMode.valueOf(scheduleMode),
            reminderOneMinutesBefore = reminderOneMinutesBefore,
            reminderTwoMinutesBefore = reminderTwoMinutesBefore,
            completed = completed,
            rawInput = rawInput,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }
}

private fun LocalTime.toMinuteOfDay(): Int = hour * 60 + minute
private fun minuteOfDayToLocalTime(totalMinutes: Int): LocalTime =
    LocalTime.of(totalMinutes / 60, totalMinutes % 60)
