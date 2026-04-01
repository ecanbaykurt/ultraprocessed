package com.b2.ultraprocessed.barcode

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MlKitBarcodeScanner(
    context: Context,
) : BarcodeScanner {
    private val appContext = context.applicationContext
    private val scanner = BarcodeScanning.getClient()

    override suspend fun scanFromImagePath(imagePath: String): BarcodeResult = withContext(Dispatchers.Default) {
        val file = File(imagePath)
        if (!file.isFile || !file.canRead()) {
            return@withContext BarcodeResult.Failure("Image file not found or unreadable.")
        }
        try {
            val image = InputImage.fromFilePath(appContext, Uri.fromFile(file))
            val barcodes = scanner.process(image).await()
            val value = barcodes.firstNotNullOfOrNull { it.rawValue?.trim()?.takeIf(String::isNotEmpty) }
            if (value == null) {
                BarcodeResult.Failure("No barcode detected in image.")
            } else {
                BarcodeResult.Success(value)
            }
        } catch (e: Exception) {
            BarcodeResult.Failure(e.message ?: "Barcode scan failed.", e)
        }
    }
}
