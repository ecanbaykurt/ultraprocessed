package com.b2.ultraprocessed.barcode

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.Executors

/**
 * CameraX preview + [ImageAnalysis] with ML Kit barcode detection. Emits at most one code per bind
 * until [unbind] (avoids duplicate navigation while the same barcode stays in view).
 */
class BarcodeLiveScanController(
    private val context: Context,
) {
    private val appContext = context.applicationContext
    private var cameraProvider: ProcessCameraProvider? = null
    private val barcodeClient = BarcodeScanning.getClient()
    private val analysisExecutor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "zest-barcode-analysis").apply { isDaemon = true }
    }

    @Volatile
    private var closed: Boolean = false

    private val delivered = AtomicBoolean(false)

    fun bind(
        previewView: PreviewView,
        lifecycleOwner: LifecycleOwner,
        onBarcodeDetected: (String) -> Unit,
        onBound: (() -> Unit)? = null,
    ) {
        closed = false
        delivered.set(false)
        val cameraProviderFuture = ProcessCameraProvider.getInstance(appContext)
        cameraProviderFuture.addListener(
            {
                val provider = cameraProviderFuture.get()
                cameraProvider = provider

                val preview = Preview.Builder().build().also { useCase ->
                    useCase.surfaceProvider = previewView.surfaceProvider
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                imageAnalysis.setAnalyzer(analysisExecutor) { imageProxy ->
                    if (closed) {
                        imageProxy.close()
                        return@setAnalyzer
                    }
                    val mediaImage = imageProxy.image
                    if (mediaImage == null) {
                        imageProxy.close()
                        return@setAnalyzer
                    }
                    val rotation = imageProxy.imageInfo.rotationDegrees
                    val image = InputImage.fromMediaImage(mediaImage, rotation)
                    barcodeClient.process(image)
                        .addOnSuccessListener { barcodes ->
                            if (closed) return@addOnSuccessListener
                            val raw = barcodes.firstNotNullOfOrNull { barcode ->
                                barcode.rawValue?.trim()?.takeIf(String::isNotEmpty)
                            }
                            if (raw != null && delivered.compareAndSet(false, true)) {
                                ContextCompat.getMainExecutor(appContext).execute {
                                    if (!closed) onBarcodeDetected(raw)
                                }
                            }
                        }
                        .addOnCompleteListener {
                            imageProxy.close()
                        }
                }

                provider.unbindAll()
                provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageAnalysis,
                )
                ContextCompat.getMainExecutor(appContext).execute {
                    if (!closed) onBound?.invoke()
                }
            },
            ContextCompat.getMainExecutor(appContext),
        )
    }

    fun unbind() {
        closed = true
        cameraProvider?.unbindAll()
        cameraProvider = null
    }
}
