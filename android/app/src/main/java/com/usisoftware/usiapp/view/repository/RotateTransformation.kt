package com.usisoftware.usiapp.view.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.widget.ImageView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.net.URL

fun loadImageWithCorrectRotation(
    context: Context,
    imageUrl: String,
    imageView: ImageView,
    placeholderRes: Int
) {
    (context as? LifecycleOwner)?.lifecycleScope?.launch(Dispatchers.IO) {
        try {
            val inputStream = URL(imageUrl).openStream()
            val byteArray = inputStream.readBytes()
            inputStream.close()

            val exif = ExifInterface(ByteArrayInputStream(byteArray))
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )

            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                ?: throw Exception("Bitmap decode edilemedi")

            val matrix = Matrix()

            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            }

            val rotatedBitmap =
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

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
