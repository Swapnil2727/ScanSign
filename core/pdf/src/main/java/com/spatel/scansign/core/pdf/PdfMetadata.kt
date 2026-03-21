package com.spatel.scansign.core.pdf

import com.tom_roush.pdfbox.pdmodel.PDDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class PdfMetadata {

    suspend fun read(pdfFile: File): Result<PdfInfo> =
        withContext(Dispatchers.IO) {
            runCatching {
                val pageCount = PDDocument.load(pdfFile).use { it.numberOfPages }
                PdfInfo(pageCount = pageCount, fileSizeBytes = pdfFile.length())
            }
        }
}

data class PdfInfo(val pageCount: Int, val fileSizeBytes: Long)
