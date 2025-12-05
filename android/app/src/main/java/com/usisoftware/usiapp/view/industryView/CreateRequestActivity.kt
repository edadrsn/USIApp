package com.usisoftware.usiapp.view.industryView

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.usisoftware.usiapp.R
import com.usisoftware.usiapp.databinding.ActivityCreateRequestBinding

class CreateRequestActivity : AppCompatActivity() {

    //SANAYİCİ TALEBİN KATEGORİSİNİ OLUŞTURUR

    private lateinit var binding: ActivityCreateRequestBinding

    private lateinit var categoryContainer: LinearLayout
    private lateinit var selectedContainer: FlexboxLayout
    private lateinit var inputCategory: EditText
    private lateinit var addButton: ImageButton

    private val selectedCategories = mutableSetOf<String>()

    // Hazır kategoriler listesi
    private val readyCategories = listOf(
        "3D Yazıcı Projeleri",
        "Akıllı Şehirler",
        "Akıllı Ev Sistemleri",
        "Artırılmış Gerçeklik",
        "Akıllı Tarım",
        "Biyoteknoloji",
        "Blokzincir",
        "Dijital Pazarlama",
        "Dil İşleme (NLP)",
        "E-Ticaret",
        "Elektrikli Araçlar",
        "Endüstri 4.0",
        "Enerji Sistemleri",
        "Eğitim Teknolojileri",
        "Fintech",
        "Girişimcilik",
        "Giyilebilir Teknolojiler",
        "Gömülü Sistemler",
        "Gıda Teknolojileri",
        "IoT(Nesnelerin İnterneti)",
        "Karar Destek Sistemleri",
        "Makine Öğrenmesi",
        "Mobil Uygulama",
        "Otomasyon Sistemleri",
        "Oyun Geliştirme",
        "Proje Yönetimi",
        "Robotik",
        "Sanal Gerçeklik",
        "Sağlık Teknolojileri",
        "Siber Güvenlik",
        "Sosyal Girişimcilik",
        "Sosyal Sorumluluk"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCreateRequestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        categoryContainer = binding.categoryContainer
        selectedContainer = binding.selectedCategoriesContainer
        inputCategory = binding.inputCategory
        addButton = binding.addButton

        // Hazır kategorileri yükle
        loadReadyCategories()

        // Ekle butonuna tıklandığında metin boş değilse yeni kategori chip olarak eklenir
        addButton.setOnClickListener {
            inputCategory.text?.toString()?.trim()?.let { text ->
                if (text.isNotEmpty() && !selectedCategories.contains(text)) {
                    addCategoryChip(text)
                    inputCategory.text?.clear()
                }
            }

        }

        //Alınan verileri RequestContentActivity sayfasına gönder
        binding.goToRequestContent.setOnClickListener {
            val intent = Intent(this@CreateRequestActivity, RequestContentActivity::class.java)
            val selectedList = ArrayList(selectedCategories)
            intent.putExtra("selectedCategories", selectedList)
            startActivity(intent)
        }
    }


    // Hazır kategorileri buton olarak FlexboxLayout içine yükle
    private fun loadReadyCategories() {
        try {
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
                        setMargins(7, 0, 7, 0)
                    }
                }

                // 3x3 sırayla yerleştir
                for (i in 0 until group.size) {
                    val category = group[i]
                    val row = i % 3
                    val column = i / 3
                    val button = MaterialButton(this).apply {
                        text = category
                        textSize = 11f
                        setTextColor(Color.BLACK)
                        setBackgroundColor(Color.parseColor("#E8ECF4"))
                        layoutParams = GridLayout.LayoutParams().apply {
                            width = GridLayout.LayoutParams.WRAP_CONTENT
                            height = GridLayout.LayoutParams.WRAP_CONTENT
                            setMargins(12, 12, 12, 12)
                            rowSpec = GridLayout.spec(row)
                            columnSpec = GridLayout.spec(column)
                        }

                        setPadding(12, 5, 12, 5)
                        setOnClickListener {
                            if (!selectedCategories.contains(category)) {
                                // Seçildi
                                selectedCategories.add(category)
                                addCategoryChip(category)

                                // Buton rengi değiştir
                                it.setBackgroundColor(Color.parseColor("#124090"))
                                (if (it is MaterialButton) it else null)?.setTextColor(Color.WHITE)
                            } else {
                                // Seçim kaldırıldı
                                selectedCategories.remove(category)
                                // Chip'i de kaldır
                                for (i in 0 until selectedContainer.childCount) {
                                    val child = selectedContainer.getChildAt(i)
                                    if (child is Chip && child.text == category) {
                                        selectedContainer.removeView(child)
                                        break
                                    }
                                }

                                // Buton rengi eski haline döndür
                                it.setBackgroundColor(Color.parseColor("#E8ECF4"))
                                (if (it is MaterialButton) it else null)?.setTextColor(Color.BLACK)
                            }
                        }

                    }
                    gridLayout.addView(button)
                }
                // Yatay LinearLayout içine grid’i ekle
                categoryContainer.addView(gridLayout)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Kategoriler yüklenirken hata oluştu", Toast.LENGTH_SHORT).show()
        }
    }


    // Seçilen kategori için Chip oluştur ve FlexboxLayout'a ekle
    private fun addCategoryChip(category: String) {

        try {
            selectedCategories.add(category)

            val chip = Chip(this).apply {
                text = category
                isCloseIconVisible = true

                // Chip stili
                chipBackgroundColor = ColorStateList.valueOf(Color.parseColor("#E6F7EC"))
                setTextColor(Color.parseColor("#1A9A50")) // daha koyu yeşil yazı
                chipStrokeColor = ColorStateList.valueOf(Color.parseColor("#1A9A50"))
                chipStrokeWidth = 1.5f
                chipCornerRadius = 40f
                textSize = 11f
                setPadding(20, 12, 20, 10)

                // Kapama ikon tasarımı
                closeIcon = ContextCompat.getDrawable(context, R.drawable.baseline_close_24)
                closeIconTint = ColorStateList.valueOf(Color.parseColor("#1A9A50"))

                elevation = 4f

                layoutParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(11, 1, 11, 1)
                }

                setOnCloseIconClickListener {
                    selectedCategories.remove(category)
                    selectedContainer.removeView(this)
                }
            }

            selectedContainer.addView(chip)
        } catch (e: Exception) {
            Log.e("CreateRequestActivity", "Kategori eklenmedi:${e.localizedMessage}")
            Toast.makeText(this, "Kategori eklenemedi", Toast.LENGTH_SHORT).show()
        }

    }

    //IndustryMainActivity sayfasına geri dön
    fun goToRequest(view: View) {
        finish()
    }
}


