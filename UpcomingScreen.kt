package cl.javier.agendaclara.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [AgendaItemEntity::class, OccurrenceStateEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AgendaDatabase : RoomDatabase() {
    abstract fun agendaDao(): AgendaDao
    abstract fun occurrenceStateDao(): OccurrenceStateDao
}
