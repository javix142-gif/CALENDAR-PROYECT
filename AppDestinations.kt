package cl.javier.agendaclara.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "agenda_items",
    indices = [Index(value = ["completed", "startDateEpochDay"])]
)
data class AgendaItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val type: String,
    val title: String,
    val notes: String?,
    val location: String?,
    val startDateEpochDay: Long,
    val startTimeMinutes: Int?,
    val endDateEpochDay: Long?,
    val endTimeMinutes: Int?,
    val durationMinutes: Int,
    val repeatRule: String,
    val scheduleMode: String,
    val reminderOneMinutesBefore: Int,
    val reminderTwoMinutesBefore: Int,
    val completed: Boolean,
    val rawInput: String?,
    val createdAt: Long,
    val updatedAt: Long,
)
