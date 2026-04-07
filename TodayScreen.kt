package cl.javier.agendaclara.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface OccurrenceStateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(state: OccurrenceStateEntity)

    @Query("SELECT * FROM occurrence_state WHERE itemId = :itemId AND occurrenceKey = :occurrenceKey LIMIT 1")
    suspend fun getState(itemId: Long, occurrenceKey: String): OccurrenceStateEntity?

    @Query("DELETE FROM occurrence_state WHERE itemId = :itemId")
    suspend fun deleteForItem(itemId: Long)
}
