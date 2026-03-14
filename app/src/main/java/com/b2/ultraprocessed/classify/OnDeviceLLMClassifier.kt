package com.b2.ultraprocessed.classify

class OnDeviceLLMClassifier(
    private val capabilityChecker: OnDeviceModelCapabilityChecker,
) : Classifier {
    override val id: String = "on_device_llm"

    override suspend fun classify(
        input: IngredientInput,
        context: ClassificationContext,
    ): ClassificationResult {
        check(capabilityChecker.isAvailable()) {
            "On-device LLM is not available on this device."
        }

        return ClassificationResult(
            novaGroup = 3,
            confidence = 0.60f,
            markers = emptyList(),
            explanation = "Stub for Gemini Nano / AICore or Android LLM inference integration.",
            highlightTerms = emptyList(),
            engine = id,
        )
    }
}

fun interface OnDeviceModelCapabilityChecker {
    fun isAvailable(): Boolean
}
