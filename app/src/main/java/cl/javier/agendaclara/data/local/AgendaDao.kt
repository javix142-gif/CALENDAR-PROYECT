package cl.javier.agendaclara.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AgendaDao {
    @Query("SELECT * FROM agenda_items ORDER BY startDateEpochDay, startTimeMinutes")
    fun observeAll(): Flow<List<AgendaItemEntity>>

    @Query("SELECT * FROM agenda_items WHERE startDateEpochDay = :epochDay ORDER BY startTimeMinutes")
    fun observeByDate(epochDay: Long): Flow<List<AgendaItemEntity>>

    @Query("SELECT * FROM agenda_items WHERE startDateEpochDay BETWEEN :from AND :to ORDER BY startDateEpochDay, startTimeMinutes")
    fun observeByRange(from: Long, to: Long): Flow<List<AgendaItemEntity>>

    @Query("SELECT * FROM agenda_items WHERE id = :id")
    suspend fun getById(id: Long): AgendaItemEntity?

    @Query("SELECT * FROM agenda_items WHERE completed = 0")
    suspend fun getActiveItems(): List<AgendaItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: AgendaItemEntity): Long

    @Update
    suspend fun update(item: AgendaItemEntity)
}
