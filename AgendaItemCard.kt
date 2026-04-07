package cl.javier.agendaclara.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AgendaDao {
    @Query("SELECT * FROM agenda_items WHERE completed = 0 ORDER BY startDateEpochDay ASC, startTimeMinutes ASC")
    fun observeActiveItems(): Flow<List<AgendaItemEntity>>

    @Query("SELECT * FROM agenda_items WHERE completed = 0 ORDER BY startDateEpochDay ASC, startTimeMinutes ASC")
    suspend fun getActiveItems(): List<AgendaItemEntity>

    @Query("SELECT * FROM agenda_items WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): AgendaItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: AgendaItemEntity): Long

    @Update
    suspend fun update(item: AgendaItemEntity)

    @Delete
    suspend fun delete(item: AgendaItemEntity)

    @Query("UPDATE agenda_items SET completed = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun markCompleted(id: Long, updatedAt: Long)
}
