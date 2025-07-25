package com.example.usiapp.view.view

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.usiapp.R
import com.example.usiapp.databinding.ActivityCreateRequestBinding
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip

class CreateRequestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateRequestBinding

    private lateinit var categoryContainer: LinearLayout
    private lateinit var selectedContainer: FlexboxLayout
    private lateinit var inputCategory: EditText
    private lateinit var addButton: ImageButton

    private val selectedCategories = mutableSetOf<String>()

    // Hazır kategoriler listesi
    private val readyCategories = listOf(
        "3D Yazıcı Projeleri", "Akıllı Şehirler", "Akıllı Ev Sistemleri",
        "Artırılmış Gerçeklik", "Akıllı Tarım", "Biyoteknoloji",
        "Blokzincir", "Dijital Pazarlama", "Dil İşleme (NLP)","E-Ticaret","Elektrikli Araçlar",
        "Endüstri 4.0","Enerji Sistemleri","Eğitim Teknolojileri","Fintech","Girişimcilik","Giyilebilir Teknolojiler",
        "Gömülü Sistemler","Gıda Teknolojileri","IoT(Nesnelerin İnterneti)","Karar Destek Sistemleri","Makine Öğrenmesi",
        "Mobil Uygulama","Otomasyon Sistemleri","Oyun Geliştirme","Proje Yönetimi","Robotik","Sanal Gerçeklik","Sağlık Teknolojileri",
        "Siber Güvenlik","Sosyal Girişimcilik","Sosyal Sorumluluk",
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCreateRequestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        categoryContainer = binding.categoryContainer
        selectedContainer = binding.selectedCategoriesContainer
        inputCategory = binding.inputCategory
        addButton = findViewById(R.id.addButton)

        // Hazır kategorileri yükle
        loadReadyCategories()

        // Ekle butonuna tıklandığında metin boş değilse yeni kategori chip olarak eklenir
        addButton.setOnClickListener {
            val text = inputCategory.text.toString().trim()
            if (text.isNotEmpty() && !selectedCategories.contains(text)) {
                addCategoryChip(text)
                inputCategory.text.clear()
            }
        }

        //İleri git ve alınan verileri gönder
        binding.goToRequestContent.setOnClickListener {
            val intent=Intent(this@CreateRequestActivity,RequestContentActivity::class.java)
            val selectedList = ArrayList(selectedCategories)
            intent.putExtra("categories",selectedList)
            startActivity(intent)
        }
    }


    // Hazır kategorileri buton olarak FlexboxLayout içine yükler
    private fun loadReadyCategories() {
        val chunkedGroups = readyCategories.chunked(9) // Her 9 kategori bir grup

        for (group in chunkedGroups) {
            // 3x3 görünüm için GridLayout
            val gridLayout = GridLayout(this).apply {
                rowCount = 3
                columnCount = 3
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(15, 0, 15, 0)
                }
            }

            // Burada 3x3 sırayla yerleştiriyoruz
            for (i in 0 until group.size) {
                val category = group[i]
                val row = i % 3
                val column = i / 3

                val button = MaterialButton(this).apply {
                    text = category
                    textSize = 11f
                    setTextColor(Color.WHITE)
                    setBackgroundColor(Color.parseColor("#1A9AAF"))
                    layoutParams = GridLayout.LayoutParams().apply {
                        width = GridLayout.LayoutParams.WRAP_CONTENT
                        height = GridLayout.LayoutParams.WRAP_CONTENT
                        setMargins(12, 12, 12, 12)
                        rowSpec = GridLayout.spec(row)
                        columnSpec = GridLayout.spec(column)
                    }

                    setPadding(24, 12, 24, 12)

                    setOnClickListener {
                        if (!selectedCategories.contains(category)) {
                            addCategoryChip(category)
                        }
                    }
                }

                gridLayout.addView(button)
            }

            // Yatay LinearLayout içine grid’i ekle
            categoryContainer.addView(gridLayout)
        }
    }



    // Seçilen kategori için Chip oluşturur ve FlexboxLayout'a ekler
    private fun addCategoryChip(category: String) {
        selectedCategories.add(category)

        val chip = Chip(this).apply {
            text = category
            isCloseIconVisible = true

            // Chip stili
            chipBackgroundColor = ColorStateList.valueOf(Color.parseColor("#E6F7EC")) // açık yeşil ton
            setTextColor(Color.parseColor("#1A9A50")) // daha koyu yeşil yazı
            chipStrokeColor = ColorStateList.valueOf(Color.parseColor("#1A9A50"))
            chipStrokeWidth = 1.5f
            chipCornerRadius = 40f
            textSize = 13f
            setPadding(20, 10, 20, 10)

            // Kapama ikon tasarımı (çarpı işareti)
            closeIcon = ContextCompat.getDrawable(context, R.drawable.baseline_close_24) // senin drawable’da varsa
            closeIconTint = ColorStateList.valueOf(Color.parseColor("#1A9A50"))

            elevation = 4f

            setOnCloseIconClickListener {
                selectedCategories.remove(category)
                selectedContainer.removeView(this)
            }
        }

        selectedContainer.addView(chip)

    }

    //Geri dön
    fun goToRequest(view: View) {
        val intent = Intent(this, IndustryMainActivity::class.java)
        intent.putExtra("goToFragment", "request")
        startActivity(intent)
        finish()
    }


}


