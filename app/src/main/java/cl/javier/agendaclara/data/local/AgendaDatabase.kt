package cl.javier.agendaclara.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [AgendaItemEntity::class], version = 1)
abstract class AgendaDatabase : RoomDatabase() {
    abstract fun agendaDao(): AgendaDao
}
