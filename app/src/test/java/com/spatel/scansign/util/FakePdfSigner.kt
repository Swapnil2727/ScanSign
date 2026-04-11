package com.spatel.scansign.util

import android.graphics.Bitmap
import com.spatel.scansign.core.pdf.PdfSigner
import java.io.File

class FakePdfSigner(private val shouldFail: Boolean = false) : PdfSigner() {

    var lastDestPdf: File? = null
    var callCount = 0

    override suspend fun embedBitmap(
        sourcePdf: File,
        destPdf: File,
        signatureBitmap: Bitmap,
        pageIndex: Int,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
    ): Result<File> {
        callCount++
        lastDestPdf = destPdf
        return if (shouldFail) Result.failure(RuntimeException("PDFBox write error"))
        else Result.success(destPdf)
    }
}
