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
                context.contentResolver.openInputStream(sourceUri)!!.use { input ->
                    input.copyTo(destinationFile.outputStream())
                }
                destinationFile
            }
        }
}
