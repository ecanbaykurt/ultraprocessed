package com.b2.ultraprocessed.classify

data class IngredientInput(
    val rawText: String,
    val normalizedText: String,
    val languageTag: String? = null,
)

data class ClassificationContext(
    val allowNetwork: Boolean,
    val apiFallbackEnabled: Boolean,
    val preferOnDevice: Boolean,
)

data class ClassificationResult(
    val novaGroup: Int,
    val confidence: Float,
    val markers: List<String>,
    val explanation: String,
    val highlightTerms: List<String>,
    val engine: String,
)

interface Classifier {
    val id: String

    suspend fun classify(
        input: IngredientInput,
        context: ClassificationContext,
    ): ClassificationResult
}
