package cl.javier.agendaclara.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "agenda_items")
data class AgendaItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val title: String,
    val notes: String?,
    val location: String?,
    val startDateEpochDay: Long,
    val startTimeMinutes: Int?,
    val endDateEpochDay: Long?,
    val endTimeMinutes: Int?,
    val durationMinutes: Int,
    val allDay: Boolean,
    val repeatRule: String,
    val reminderOneMinutesBefore: Int,
    val reminderTwoMinutesBefore: Int,
    val completed: Boolean,
    val hasExactTime: Boolean,
    val rawInput: String?,
    val createdAtEpochSec: Long,
    val updatedAtEpochSec: Long,
)
