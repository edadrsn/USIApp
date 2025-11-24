package com.usisoftware.usiapp.view.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.widget.ImageView
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

fun loadImageWithCorrectRotation(context: Context, imageUrl: String, imageView: ImageView, placeholderRes: Int) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            // Firebase URL veya herhangi bir URL'den InputStream alıyoruz
            val inputStream = URL(imageUrl).openStream()
            val exif = android.media.ExifInterface(inputStream)

            // EXIF yönünü oku
            val orientation = exif.getAttributeInt(
                android.media.ExifInterface.TAG_ORIENTATION,
                android.media.ExifInterface.ORIENTATION_NORMAL
            )

            // Bitmap oluştur
            val bitmap = BitmapFactory.decodeStream(URL(imageUrl).openStream())
            val matrix = Matrix()

            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            }

            val rotatedBitmap = Bitmap.createBitmap(
                bitmap,
                0,
                0,
                bitmap.width,
                bitmap.height,
                matrix,
                true
            )

            // Ana thread’de ImageView’e uygula
            withContext(Dispatchers.Main) {
                Glide.with(context)
                    .load(rotatedBitmap)
                    .placeholder(placeholderRes)
                    .error(placeholderRes)
                    .into(imageView)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Glide.with(context)
                    .load(placeholderRes)
                    .into(imageView)
            }
        }
    }
}
