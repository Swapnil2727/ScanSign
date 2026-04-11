package com.spatel.scansign.core.pdf

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

open class PdfPageRenderer {

    open suspend fun renderPage(pdfFile: File, pageIndex: Int, widthPx: Int): Result<Bitmap> =
        withContext(Dispatchers.IO) {
            runCatching {
                val pfd = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
                val renderer = PdfRenderer(pfd)
                try {
                    val page = renderer.openPage(pageIndex)
                    try {
                        val heightPx = (widthPx.toFloat() / page.width * page.height).toInt()
                        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
                        Canvas(bitmap).drawColor(Color.WHITE)
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        bitmap
                    } finally {
                        page.close()
                    }
                } finally {
                    renderer.close()
                    pfd.close()
                }
            }
        }

    open suspend fun getPageSizePt(pdfFile: File, pageIndex: Int): Result<Pair<Int, Int>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val pfd = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
                val renderer = PdfRenderer(pfd)
                try {
                    val page = renderer.openPage(pageIndex)
                    try {
                        Pair(page.width, page.height)
                    } finally {
                        page.close()
                    }
                } finally {
                    renderer.close()
                    pfd.close()
                }
            }
        }
}
