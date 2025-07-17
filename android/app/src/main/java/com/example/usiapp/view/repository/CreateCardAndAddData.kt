package com.example.usiapp.view.repository

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.usiapp.R
import com.google.firebase.firestore.FirebaseFirestore



class CreateCardAndAddData(
    private val context: Context,
    private val container: LinearLayout,
    private val db: FirebaseFirestore,
    private val documentId: String,
    private val listKey: String,
    private val itemList: MutableList<String>,
    private val noDataTextView: TextView? = null
) {

    // Kart oluştur
    fun createCard(item: String) {
        // Kart için yatay LinearLayout
        val cardLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 150
            ).apply {
                // Karta dış kenar boşlukları verdim
                setMargins(25, 22, 25, 0)
            }
            background = ContextCompat.getDrawable(context, R.drawable.rounded_bg)
            setPadding(24, 24, 24, 24)
        }

        // Yazı için dikey LinearLayout
        val textLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        // Kartta gösterilecek yazı
        val itemName = TextView(context).apply {
            text = item
            setTextColor(Color.BLACK)
            textSize = 17f
            gravity = Gravity.CENTER_VERTICAL // Yazıyı dikey ortalar
        }

        // TextView'i textLayout'a ekle
        textLayout.addView(itemName)

        // Silme iconu
        val deleteButton = ImageButton(context).apply {
            setImageResource(R.drawable.baseline_delete_24) // İkon resmi
            setBackgroundColor(Color.TRANSPARENT) // Arkaplanı şeffaf yap
            layoutParams = LinearLayout.LayoutParams(70, 70).apply {
                gravity = Gravity.CENTER_VERTICAL // Ortala
            }

            // Silme butonuna tıklama
            setOnClickListener {
                AlertDialog.Builder(context).apply {
                    setTitle("Bilgi silinsin mi?")
                    setMessage("Bu bilgiyi silmek istediğinize emin misiniz?")
                    setPositiveButton("Evet") { dialog, _ ->
                        // Kartı ekrandan kaldır
                        container.removeView(cardLayout)
                        // Listeden çıkar
                        itemList.remove(item)
                        // Firestore'u güncelle
                        updateToFirestore("Bilgi silindi")

                        dialog.dismiss()
                    }
                    setNegativeButton("Hayır") { dialog, _ -> dialog.dismiss() }
                    create().show()
                }
            }
        }

        // Kart yapısına textLayout ve deleteButton'u ekle
        cardLayout.addView(textLayout)
        cardLayout.addView(deleteButton)

        //kartı containera ekle
        container.addView(cardLayout)
    }

    // Yeni veri ekle
    fun addItem(newItem: String, input: EditText) {
        if (newItem.isNotBlank()) {
            // Listeye ekle
            itemList.add(newItem)

            updateToFirestore("Bilgi eklendi"){
                // Kartı oluştur ve ekranda göster
                createCard(newItem)

                // Edittexti temizle
                input.text.clear()

                // "Veri yok" yazısı varsa kaldır
                noDataTextView?.let { container.removeView(it) }
            }
        } else {
            Toast.makeText(context, "Boş bilgi eklenemez!", Toast.LENGTH_SHORT).show()
        }
    }

    // Firestore'da verileri güncelle
    private fun updateToFirestore(
        successMessage: String = "Veri başarıyla kaydedildi",
        onSuccess: (() -> Unit)? = null) {
        db.collection("AcademicianInfo").document(documentId)
            .update(listKey, itemList) // Firestore'daki ilgili alanı güncelle
            .addOnSuccessListener {
                Toast.makeText(context, successMessage, Toast.LENGTH_SHORT).show()
                onSuccess?.invoke() // Güncelleme başarılı olursa devam et
            }
            .addOnFailureListener {
                Toast.makeText(context, "Hata: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
            }

        // Eğer liste boşsa "Veri yok" yazısını göster
        if (itemList.isEmpty()) {
            noDataTextView?.let { container.addView(it) }
        }
    }

}
