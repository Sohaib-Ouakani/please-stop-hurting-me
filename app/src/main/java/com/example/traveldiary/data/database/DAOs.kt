package com.example.traveldiary.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface TripsDAO {
    @Query("SELECT * FROM trip ORDER BY name ASC")
    fun getAll(): Flow<List<Trip>>

    @Query("SELECT * FROM trip WHERE id=:id")
    fun getById(id: String): Flow<Trip?>

    @Upsert
    suspend fun upsert(trip: Trip)

    @Delete
    suspend fun delete(item: Trip)
}
