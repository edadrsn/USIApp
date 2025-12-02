package com.usisoftware.usiapp.view.studentView

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.usisoftware.usiapp.databinding.ActivityUniversityInfoBinding
import com.usisoftware.usiapp.view.repository.StudentInfo
import org.json.JSONArray
import org.json.JSONException

class UniversityInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUniversityInfoBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var universitelerListe: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUniversityInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Kullanıcı oturumu bulunamadı!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        val uid = currentUser.uid


        // assets klasöründen JSON dosyasını oku
        val jsonString = try {
            loadJsonFromAsset("universite_isimleri.json")
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Üniversite listesi yüklenemedi", Toast.LENGTH_SHORT).show()
            return
        }

        val jsonArray = try {
            JSONArray(jsonString)
        } catch (e: JSONException) {
            e.printStackTrace()
            Toast.makeText(this, "Üniversite listesi okunamadı", Toast.LENGTH_SHORT).show()
            return
        }

        val tempList = mutableListOf<String>()
        for (i in 0 until jsonArray.length()) {
            jsonArray.optString(i)?.let { tempList.add(it) }
        }
        universitelerListe = tempList


        // Adapter
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, universitelerListe)

        val actv = binding.universityName
        actv.setAdapter(adapter)

        actv.threshold = 1
        actv.isCursorVisible = false
        actv.inputType = InputType.TYPE_NULL

        // Tıklayınca/dokununca dropdown'ı aç
        actv.setOnClickListener {
            if (!actv.isPopupShowing) actv.showDropDown()
        }

        actv.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (!actv.isPopupShowing) actv.showDropDown()
                // Yine de soft keyboard varsa gizle
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(actv.windowToken, 0)
            }
            false
        }

        // Seçim yapıldığında focus'u alıp dropdown'u kapatmasını kontrol et
        actv.setOnItemClickListener { parent, view, position, id ->
            // seçildiğinde istenirse focus'u kaldır
            actv.clearFocus()
        }

        // Firebase’den öğrencinin verisini çek ve AutoCompleteTextView'e set et
        StudentInfo(db).getStudentData(
            uid,
            onSuccess = { document ->
                if (isFinishing || isDestroyed) return@getStudentData

                if (document != null && document.exists()) {
                    // ikinci parametre false => setText filtre uygulamasın
                    actv.setText(document.getString("universityName") ?: "", false)
                }
            },
            onFailure = {
                Log.e("UniversityInfoActivity","Veri çekilemedi",it)
                Toast.makeText(this, "Bir hata oluştu, lütfen tekrar deneyin.", Toast.LENGTH_SHORT).show()
            })

        // Kaydet butonu
        binding.saveUniInfo.setOnClickListener {
            val universityName = actv.text.toString().trim()
            if (universityName.isEmpty()) {
                Toast.makeText(this, "Lütfen tüm alanları doldurunuz", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            StudentInfo(db).updateStudentData(
                uid,
                hashMapOf("universityName" to universityName),
                onSuccess = {
                    Toast.makeText(this, "Bilgiler kaydedildi", Toast.LENGTH_SHORT).show()
                    finish()
                },
                onFailure = {
                    Log.e("UniversityInfo", "Firestore error", it)
                    Toast.makeText(this, "Bir hata oluştu, lütfen tekrar deneyin.", Toast.LENGTH_SHORT).show()

                })
        }
    }

    private fun loadJsonFromAsset(fileName: String): String {
        val inputStream = assets.open(fileName)
        return inputStream.bufferedReader().use { it.readText() }
    }

    fun backToProfile(view: View) {
        finish()
    }
}