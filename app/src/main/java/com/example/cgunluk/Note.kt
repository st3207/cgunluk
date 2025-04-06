package com.example.cgunluk // Paket adınızı kontrol edin

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(tableName = "notes", indices = [Index(value = ["dependencyTitle", "date"], unique = true)]) // Aynı başlık ve tarih için tek not
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0, // Benzersiz kimlik, otomatik artan
    val dependencyTitle: String, // Hangi bağımlılığa ait olduğu
    val date: String, // Notun tarihi (YYYY-MM-DD formatında saklamak iyi olabilir)
    var content: String // Notun içeriği (değiştirilebilir olduğu için var)
)