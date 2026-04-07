package cl.javier.agendaclara.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "occurrence_state",
    indices = [Index(value = ["itemId", "occurrenceKey"], unique = true)]
)
data class OccurrenceStateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val itemId: Long,
    val occurrenceKey: String,
    val status: String,
    val updatedAt: Long,
)
