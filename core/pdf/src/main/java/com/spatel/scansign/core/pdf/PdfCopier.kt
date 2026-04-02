package com.spatel.scansign.core.pdf

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class PdfCopier(private val context: Context) {

    suspend fun copy(sourceUri: Uri, destinationFile: File): Result<File> =
        withContext(Dispatchers.IO) {
            runCatching {
                val inputStream = if (sourceUri.scheme == "file") {
                    File(sourceUri.path!!).inputStream()
                } else {
                    context.contentResolver.openInputStream(sourceUri)!!
                }
                inputStream.use { it.copyTo(destinationFile.outputStream()) }
                destinationFile
            }
        }
}
