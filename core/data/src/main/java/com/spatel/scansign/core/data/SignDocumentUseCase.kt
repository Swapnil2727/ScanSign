package com.spatel.scansign.core.data

import android.graphics.Bitmap
import com.spatel.scansign.core.pdf.PdfPageRenderer
import com.spatel.scansign.core.pdf.PdfSigner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Embeds a signature bitmap onto a page of a document's PDF, then marks the
 * document as SIGNED in the database.
 *
 * Coordinates ([x], [y], [width], [height]) are in PDF points (1 pt = 1/72 in),
 * with the origin at the bottom-left of the page. The caller (signing UI in Week 9)
 * is responsible for converting screen coordinates to PDF space.
 */
fun interface SignDocumentUseCase {

    suspend operator fun invoke(
        documentId: String,
        signatureBitmap: Bitmap,
        pageIndex: Int,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
    ): Result<Unit>

    companion object {
        operator fun invoke(
            documentRepository: DocumentRepository,
            pdfSigner: PdfSigner,
            pdfPageRenderer: PdfPageRenderer,
        ): SignDocumentUseCase = SignDocumentUseCase { documentId, bitmap, pageIndex, x, y, w, h ->
            withContext(Dispatchers.IO) {
                runCatching {
                    val document = documentRepository.getById(documentId).firstOrNull()
                        ?: error("Document $documentId not found")
                    val pdfPath = document.pdfPath
                        ?: error("Document $documentId has no PDF path")
                    val pdfFile = File(pdfPath)

                    pdfSigner.embedBitmap(
                        sourcePdf = pdfFile,
                        destPdf = pdfFile, // modify in place
                        signatureBitmap = bitmap,
                        pageIndex = pageIndex,
                        x = x,
                        y = y,
                        width = w,
                        height = h,
                    ).getOrThrow()

                    // Re-render the signed page and overwrite its image file so that
                    // DocumentDetailScreen and DocumentViewerScreen show the embedded signature.
                    // Coil uses File.lastModified() as a cache key, so it picks up the new bytes.
                    val signedPage = documentRepository.getPages(documentId).getOrNull(pageIndex)
                    if (signedPage != null && signedPage.imagePath.isNotEmpty()) {
                        pdfPageRenderer.renderPage(pdfFile, pageIndex, widthPx = 1080)
                            .getOrNull()
                            ?.let { updated ->
                                File(signedPage.imagePath).outputStream().use { out ->
                                    updated.compress(Bitmap.CompressFormat.JPEG, 95, out)
                                }
                            }
                    }

                    documentRepository.markAsSigned(documentId)
                }
            }
        }
    }
}
