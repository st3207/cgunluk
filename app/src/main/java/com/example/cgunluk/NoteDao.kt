package com.example.cgunluk // Paket adınızı kontrol edin

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface NoteDao {

    // Yeni not ekle veya var olanı güncelle (aynı dependencyTitle ve date için)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateNote(note: Note) // suspend: Coroutine içinde çalışacak

    // Belirli bir bağımlılık ve tarihe ait notu getir
    @Query("SELECT * FROM notes WHERE dependencyTitle = :title AND date = :date LIMIT 1")
    suspend fun getNoteByTitleAndDate(title: String, date: String): Note? // suspend, Note? (null olabilir)

    // Belirli bir bağımlılığa ait tüm notları getir (ileride gerekebilir)
    @Query("SELECT * FROM notes WHERE dependencyTitle = :title ORDER BY date DESC")
    suspend fun getAllNotesForTitle(title: String): List<Note> // suspend

    // Not güncelleme (alternatif)
    @Update
    suspend fun updateNote(note: Note) // suspend
}