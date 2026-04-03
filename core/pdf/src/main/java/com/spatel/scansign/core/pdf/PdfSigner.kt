package com.spatel.scansign.core.pdf

import android.graphics.Bitmap
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.graphics.image.JPEGFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File

/**
 * Embeds a signature bitmap onto a specific page of an existing PDF.
 *
 * Coordinate system: [x] and [y] are in PDF points (1 pt = 1/72 in),
 * measured from the **bottom-left** of the page. [width] and [height]
 * are also in points. The caller is responsible for mapping screen/UI
 * coordinates to PDF space before calling this.
 */
open class PdfSigner {

    /**
     * Opens [sourcePdf], draws [signatureBitmap] at the given position on [pageIndex],
     * and writes the result to [destPdf]. [sourcePdf] and [destPdf] may be the same file.
     *
     * @param pageIndex   zero-based page index
     * @param x           left edge of the signature in PDF points from bottom-left
     * @param y           bottom edge of the signature in PDF points from bottom-left
     * @param width       signature width in PDF points
     * @param height      signature height in PDF points
     */
    open suspend fun embedBitmap(
        sourcePdf: File,
        destPdf: File,
        signatureBitmap: Bitmap,
        pageIndex: Int,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            require(pageIndex >= 0) { "pageIndex must be >= 0" }
            require(width > 0 && height > 0) { "width and height must be positive" }

            val jpegBytes = ByteArrayOutputStream().also { out ->
                signatureBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
            }.toByteArray()

            val document = PDDocument.load(sourcePdf)
            try {
                require(pageIndex < document.numberOfPages) {
                    "pageIndex $pageIndex out of range (${document.numberOfPages} pages)"
                }
                val page = document.getPage(pageIndex)
                val pdImage = JPEGFactory.createFromStream(
                    document,
                    ByteArrayInputStream(jpegBytes),
                )
                PDPageContentStream(
                    document,
                    page,
                    PDPageContentStream.AppendMode.APPEND,
                    true, // compress
                ).use { cs ->
                    cs.drawImage(pdImage, x, y, width, height)
                }
                document.save(destPdf)
            } finally {
                document.close()
            }
            destPdf
        }
    }

    companion object {
        private const val JPEG_QUALITY = 90
    }
}
