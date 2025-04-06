package com.example.cgunluk // Paket adınızı kontrol edin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.cgunluk.databinding.ActivityMainBinding // Otomatik oluşturulan binding sınıfı

class MainActivity : AppCompatActivity() {

    // View Binding için değişken tanımlama
    private lateinit var binding: ActivityMainBinding
    // SharedPreferences anahtarları
    private val PREFS_NAME = "cgunluk_prefs"
    private val DEPENDENCIES_KEY = "dependencies_list"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // View Binding'i başlatma
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // Sistem çubukları (status bar, navigation bar) için padding ayarlama (genellikle otomatik gelir)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Kaydedilmiş bağımlılıkları yükle ve göster
        loadAndDisplayDependencies()

        // "Bağımlılık Ekle" butonu tıklama olayı
        binding.buttonAddDependency.setOnClickListener {
            showAddDependencyDialog()
        }
    }

    // Yeni bağımlılık ekleme dialogunu gösteren fonksiyon
    private fun showAddDependencyDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Yeni Bağımlılık Başlığı Girin")

        // Dialog içine bir EditText (giriş alanı) ekle
        val input = EditText(this)
        input.hint = "Örn: Sigarayı Bırakma"
        builder.setView(input)

        // "Ekle" butonu
        builder.setPositiveButton("Ekle") { dialog, _ ->
            val dependencyTitle = input.text.toString().trim()
            if (dependencyTitle.isNotEmpty()) {
                // Yeni başlığı ekle ve kaydet
                addDependencyToList(dependencyTitle)
                saveDependencies() // Kaydetme işlemini çağır
            }
            dialog.dismiss()
        }

        // "İptal" butonu
        builder.setNegativeButton("İptal") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    // Başlığı listeye (LinearLayout) ekleyen fonksiyon
    private fun addDependencyToList(title: String, shouldSave: Boolean = false) {
        val textView = TextView(this).apply {
            text = title
            textSize = 18f // sp yerine f kullanmak gerekebilir kod içinde
            setPadding(0, 8, 0, 8) // Biraz boşluk ekleyelim
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            // Tıklanabilir yap ve tıklanınca Günlük Aktivitesini aç
            isClickable = true
            isFocusable = true
            setOnClickListener {
                // Günlük Aktivitesini başlat
                val intent = Intent(this@MainActivity, GunlukActivity::class.java)
                // Başlığı diğer aktiviteye gönder
                intent.putExtra("DEPENDENCY_TITLE", title)
                startActivity(intent)
            }
        }
        binding.linearLayoutDependencies.addView(textView)

        // Eğer çağrılırken belirtilmişse (yeni ekleniyorsa) SharedPreferences'a kaydet
        if (shouldSave) {
            saveDependencies()
        }
    }

    // Mevcut bağımlılıkları SharedPreferences'a kaydeden fonksiyon
    private fun saveDependencies() {
        val dependencies = mutableSetOf<String>()
        for (i in 0 until binding.linearLayoutDependencies.childCount) {
            val textView = binding.linearLayoutDependencies.getChildAt(i) as? TextView
            textView?.let {
                dependencies.add(it.text.toString())
            }
        }
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        prefs.edit().putStringSet(DEPENDENCIES_KEY, dependencies).apply()
    }

    // SharedPreferences'dan bağımlılıkları yükleyip gösteren fonksiyon
    private fun loadAndDisplayDependencies() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val dependencies = prefs.getStringSet(DEPENDENCIES_KEY, setOf()) ?: setOf()

        // Önce mevcut listeyi temizle (varsa)
        binding.linearLayoutDependencies.removeAllViews()

        dependencies.forEach { title ->
            addDependencyToList(title, false) // Yüklerken tekrar kaydetme
        }
    }
}