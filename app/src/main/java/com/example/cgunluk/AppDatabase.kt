package com.example.cgunluk // Paket adınızı kontrol edin

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Note::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao // NoteDao'ya erişim sağlayan abstract fonksiyon

    companion object {
        // Singleton pattern: Veritabanı nesnesinin sadece bir örneğinin olmasını sağlar
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // Eğer INSTANCE null ise oluştur, değilse mevcut olanı döndür
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cgunluk_database" // Veritabanı dosyasının adı
                )
                    // Migration stratejisi eklenebilir (versiyon yükseltmeleri için)
                    // .fallbackToDestructiveMigration() // Şimdilik basit tutalım, versiyon değişirse veritabanını silip yeniden oluşturur
                    .build()
                INSTANCE = instance
                // return instance
                instance // Kotlin style return
            }
        }
    }
}