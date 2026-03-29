package com.spatel.scansign.core.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.graphics.image.JPEGFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File

class ImagesToPdfConverter(private val context: Context) {

    companion object {
        private const val MAX_IMAGE_PX = 1600
        private const val JPEG_QUALITY = 85
    }

    suspend fun convert(imageUris: List<Uri>): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            require(imageUris.isNotEmpty()) { "No images provided" }
            val outDir = File(context.cacheDir, "gallery_import").also { it.mkdirs() }
            val destFile = File(outDir, "${System.currentTimeMillis()}.pdf")

            val document = PDDocument()
            try {
                imageUris.forEach { uri ->
                    val bitmap = loadAndOrientBitmap(uri)
                    val w = bitmap.width.toFloat()
                    val h = bitmap.height.toFloat()

                    val page = PDPage(PDRectangle(w, h))
                    document.addPage(page)

                    val jpegBytes = ByteArrayOutputStream().also { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
                    }.toByteArray()
                    bitmap.recycle()

                    val pdImage = JPEGFactory.createFromStream(document, ByteArrayInputStream(jpegBytes))
                    PDPageContentStream(document, page).use { cs ->
                        cs.drawImage(pdImage, 0f, 0f, w, h)
                    }
                }
                document.save(destFile)
            } finally {
                document.close()
            }
            destFile
        }
    }

    private fun loadAndOrientBitmap(uri: Uri): Bitmap {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, bounds)
        }

        val opts = BitmapFactory.Options().apply {
            inSampleSize = calculateSampleSize(bounds.outWidth, bounds.outHeight)
        }
        val bitmap = context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, opts)
        } ?: error("Cannot decode image: $uri")

        val rotation = readExifRotation(uri)
        return if (rotation != 0) {
            val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                .also { bitmap.recycle() }
        } else {
            bitmap
        }
    }

    private fun calculateSampleSize(width: Int, height: Int): Int {
        var size = 1
        while (maxOf(width, height) / (size * 2) >= MAX_IMAGE_PX) size *= 2
        return size
    }

    private fun readExifRotation(uri: Uri): Int = try {
        context.contentResolver.openInputStream(uri)?.use { input ->
            val exif = ExifInterface(input)
            when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } ?: 0
    } catch (_: Exception) {
        0
    }
}
