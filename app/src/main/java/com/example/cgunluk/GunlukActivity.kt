package com.example.cgunluk // Paket adınızı kontrol edin

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope // CoroutineScope için
import com.example.cgunluk.databinding.ActivityGunlukBinding // View Binding
import kotlinx.coroutines.launch // Coroutine başlatmak için
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import android.content.Context
class GunlukActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGunlukBinding
    private lateinit var db: AppDatabase
    private lateinit var noteDao: NoteDao
    private lateinit var gestureDetector: GestureDetectorCompat // Swipe algılama için

    private lateinit var currentDependencyTitle: String
    private val calendar: Calendar = Calendar.getInstance() // Tarih işlemleri için
    private var initialStartDateMillis: Long = 0L // Bağımlılığın başlangıç tarihi (milisaniye)
    private var isNoteLoaded = false // Notun yüklenip yüklenmediğini kontrol etmek için flag

    // Tarih formatı (YYYY-MM-DD) - Veritabanı kaydı ve karşılaştırma için
    private val dbDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    // Gösterilecek tarih formatı (örnek: 06 Nisan 2025 Cumartesi)
    private val displayDateFormat = SimpleDateFormat("dd MMMM yyyy EEEE", Locale("tr", "TR")) // Türkçe lokasyon

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGunlukBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Veritabanı bağlantısını kur
        db = AppDatabase.getDatabase(applicationContext)
        noteDao = db.noteDao()

        // MainActivity'den gönderilen başlığı al
        currentDependencyTitle = intent.getStringExtra("DEPENDENCY_TITLE") ?: "Başlık Yok"
        // Başlığa göre başlangıç tarihini yükle (veya ilk kezse kaydet)
        loadOrCreateStartDate()


        // Geri tuşu işlevi
        binding.imageViewBack.setOnClickListener {
            finish() // Bu aktiviteyi kapatıp öncekine (MainActivity) dön
        }

        // Mevcut tarihi göster ve notu yükle
        updateDateAndLoadNote()

        // Not alanındaki değişiklikleri dinle ve otomatik kaydet
        binding.editTextNotes.addTextChangedListener { editable ->
            // Not yüklendikten sonra yapılan değişiklikleri kaydet
            if (isNoteLoaded) {
                saveCurrentNote()
            }
        }

        // Swipe (kaydırma) algılayıcısını ayarla
        setupSwipeDetector()
    }

    // Başlangıç tarihini SharedPreferences'dan yükle veya o anki tarihi kaydet
    private fun loadOrCreateStartDate() {
        val prefs = getSharedPreferences("cgunluk_start_dates", Context.MODE_PRIVATE)
        val key = "start_date_$currentDependencyTitle" // Her başlık için ayrı anahtar
        initialStartDateMillis = prefs.getLong(key, 0L)

        if (initialStartDateMillis == 0L) {
            // Eğer bu başlık için başlangıç tarihi kaydedilmemişse, o anki tarihi kaydet
            initialStartDateMillis = Calendar.getInstance().apply {
                // Saati, dakikayı, saniyeyi sıfırlayarak sadece günün başlangıcını alalım
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            prefs.edit().putLong(key, initialStartDateMillis).apply()
        }
    }


    // Tarih gösterimini ve gün sayısını güncelleyen, ilgili notu yükleyen fonksiyon
    private fun updateDateAndLoadNote() {
        isNoteLoaded = false // Yeni not yüklenirken kaydetmeyi engelle
        val currentDateStr = displayDateFormat.format(calendar.time)
        binding.textViewDate.text = currentDateStr

        // Gün sayısını hesapla
        val dayCount = calculateDayDifference(initialStartDateMillis, calendar.timeInMillis) + 1 // +1 çünkü ilk gün 1. gün
        binding.textViewDayCount.text = "($dayCount)"

        // Veritabanından notu yükle (Coroutine içinde)
        val dateForDb = dbDateFormat.format(calendar.time) // YYYY-MM-DD formatı
        lifecycleScope.launch { // Arka planda çalıştır
            val note = noteDao.getNoteByTitleAndDate(currentDependencyTitle, dateForDb)
            binding.editTextNotes.setText(note?.content ?: "") // Not varsa içeriğini, yoksa boş string ata
            isNoteLoaded = true // Not yüklendi, artık değişiklikler kaydedilebilir
        }
    }

    // Mevcut notu veritabanına kaydeden fonksiyon
    private fun saveCurrentNote() {
        val dateForDb = dbDateFormat.format(calendar.time)
        val content = binding.editTextNotes.text.toString()

        val note = Note(
            dependencyTitle = currentDependencyTitle,
            date = dateForDb,
            content = content
        )

        // Coroutine içinde veritabanına yaz
        lifecycleScope.launch {
            noteDao.insertOrUpdateNote(note)
            // İsteğe bağlı: Kaydedildiğine dair küçük bir bildirim
            // Toast.makeText(this@GunlukActivity, "Not kaydedildi", Toast.LENGTH_SHORT).show()
        }
    }

    // İki tarih arasındaki gün farkını hesaplayan fonksiyon
    private fun calculateDayDifference(startDateMillis: Long, endDateMillis: Long): Long {
        val startCal = Calendar.getInstance().apply { timeInMillis = startDateMillis; normalizeTime() }
        val endCal = Calendar.getInstance().apply { timeInMillis = endDateMillis; normalizeTime() }

        val diffMillis = endCal.timeInMillis - startCal.timeInMillis
        return diffMillis / (1000 * 60 * 60 * 24) // Milisaniyeyi güne çevir
    }

    // Takvim saatini gece yarısına normalize eden yardımcı fonksiyon
    private fun Calendar.normalizeTime() {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }


    // Swipe (Kaydırma) algılamasını ayarlayan fonksiyon
    @SuppressLint("ClickableViewAccessibility") // Dokunma olaylarını dinlemek için
    private fun setupSwipeDetector() {
        val listener = object : GestureDetector.SimpleOnGestureListener() {
            private val SWIPE_THRESHOLD = 100 // Kaydırma olarak kabul edilecek min piksel
            private val SWIPE_VELOCITY_THRESHOLD = 100 // Min hız

            override fun onFling(
                e1: MotionEvent?, // Başlangıç noktası
                e2: MotionEvent, // Bitiş noktası
                velocityX: Float, // X eksenindeki hız
                velocityY: Float  // Y eksenindeki hız
            ): Boolean {
                if (e1 == null) return false
                val diffX = e2.x - e1.x
                val diffY = e2.y - e1.y

                if (abs(diffX) > abs(diffY)) { // Yatay kaydırma
                    if (abs(diffX) > SWIPE_THRESHOLD && abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            // Sağdan sola (->) kaydırma: Önceki gün
                            changeDay(-1)
                        } else {
                            // Soldan sağa (<-) kaydırma: Sonraki gün
                            changeDay(1)
                        }
                        return true // Olay işlendi
                    }
                }
                return false // Olay işlenmedi
            }
            // Dokunma olayının aktivite tarafından işlenmesi için onDown'da true döndürmek önemli
            override fun onDown(e: MotionEvent): Boolean {
                return true
            }
        }
        gestureDetector = GestureDetectorCompat(this, listener)

        // Dokunma olaylarını EditText yerine ConstraintLayout'un dinlemesini sağla
        // (EditText kendi kaydırmasını yönetir)
        binding.gunlukMain.setOnTouchListener { _, event ->
            // Dokunma olayını gestureDetector'a ilet
            // Eğer dokunulan yer EditText ise, olayı EditText'e bırak, değilse algılayıcıya ver
            val editTextRect = android.graphics.Rect()
            binding.editTextNotes.getHitRect(editTextRect)
            if (editTextRect.contains(event.x.toInt(), event.y.toInt())) {
                // EditText'e dokunuldu, normal davranışına izin ver (kaydırma vb.)
                // gestureDetector'a gönderme ki EditText'in kendi scroll'u çalışsın
                false // Olay işlenmedi, EditText'e devam etsin
            } else {
                // EditText dışına dokunuldu, swipe kontrolü yap
                gestureDetector.onTouchEvent(event)
                true // Olay gestureDetector tarafından işlendi (veya işlenmeye çalışıldı)
            }
        }
    }

    // Günü değiştiren fonksiyon (pozitif: ileri, negatif: geri)
    private fun changeDay(amount: Int) {
        // Notu kaydet (günü değiştirmeden önce)
        if (isNoteLoaded) { // Sadece not yüklendiyse kaydet
            saveCurrentNote()
        }

        // Takvimi güncelle
        calendar.add(Calendar.DAY_OF_YEAR, amount)

        // Yeni tarihi göster ve yeni günün notunu yükle
        updateDateAndLoadNote()

        // Kullanıcıya hangi güne geçildiğini bildirebiliriz (isteğe bağlı)
        val direction = if (amount > 0) "Sonraki" else "Önceki"
        Toast.makeText(this, "$direction güne geçildi", Toast.LENGTH_SHORT).show()
    }

}