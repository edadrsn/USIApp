package com.usisoftware.usiapp.view.repository

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
import com.google.firebase.firestore.FirebaseFirestore
import com.usisoftware.usiapp.R

class CreateCardAndAddData(
    private val context: Context,
    private val container: LinearLayout,
    private val db: FirebaseFirestore,
    private val userId: String,
    private val listKey: String,
    private val itemList: MutableList<String>,
    private val noDataTextView: TextView? = null
) {

    // Kart oluştur
    fun createCard(item: String) {
        val cardLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(25, 22, 25, 0)
            }
            background = ContextCompat.getDrawable(context, R.drawable.rounded_bg)
            setPadding(24, 24, 24, 24)
        }

        val textLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        val itemName = TextView(context).apply {
            text = item
            setTextColor(Color.BLACK)
            textSize = 15f
            gravity = Gravity.CENTER_VERTICAL
        }

        textLayout.addView(itemName)

        // Silme butonu
        val deleteButton = ImageButton(context).apply {
            setImageResource(R.drawable.baseline_delete_24)
            setBackgroundColor(Color.TRANSPARENT)
            layoutParams = LinearLayout.LayoutParams(70, 70).apply {
                gravity = Gravity.CENTER_VERTICAL
            }

            setOnClickListener {
                AlertDialog.Builder(context).apply {
                    setTitle("Bilgi silinsin mi?")
                    setMessage("Bu bilgiyi silmek istediğinize emin misiniz?")
                    setPositiveButton("Evet") { dialog, _ ->
                        container.removeView(cardLayout)
                        itemList.remove(item)

                        updateToFirestore("Bilgi silindi")
                        dialog.dismiss()
                    }
                    setNegativeButton("Hayır") { dialog, _ -> dialog.dismiss() }
                    create().show()
                }
            }
        }

        cardLayout.addView(textLayout)
        cardLayout.addView(deleteButton)

        container.addView(cardLayout)
    }

    // Yeni veri ekle
    fun addItem(newItem: String, input: EditText) {
        if (newItem.isNotBlank()) {

            itemList.add(newItem)

            updateToFirestore("Bilgi eklendi") {
                createCard(newItem)
                input.text.clear()
                noDataTextView?.let { container.removeView(it) }
            }

        } else {
            Toast.makeText(context, "Boş bilgi eklenemez!", Toast.LENGTH_SHORT).show()
        }
    }

    // Firestore güncelleme
    private fun updateToFirestore(
        successMessage: String = "Veri başarıyla kaydedildi",
        onSuccess: (() -> Unit)? = null
    ) {
        db.collection("Academician")
            .document(userId)
            .update(listKey, itemList)
            .addOnSuccessListener {
                Toast.makeText(context, successMessage, Toast.LENGTH_SHORT).show()
                onSuccess?.invoke()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Hata: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        if (itemList.isEmpty()) {
            noDataTextView?.let {
                if (it.parent == null) {
                    container.addView(it)
                }
            }
        }


        if (itemList.isEmpty()) {
            noDataTextView?.let { container.addView(it) }
        }
    }
}
