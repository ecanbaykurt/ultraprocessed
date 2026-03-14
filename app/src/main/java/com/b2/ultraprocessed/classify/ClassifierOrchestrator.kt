package com.b2.ultraprocessed.classify

enum class EngineMode {
    Auto,
    OnDeviceOnly,
    ApiOnly,
}

class ClassifierOrchestrator(
    private val rulesClassifier: RulesClassifier,
    private val onDeviceClassifier: OnDeviceLLMClassifier?,
    private val apiClassifier: ApiLLMClassifier?,
) {
    suspend fun classify(
        input: IngredientInput,
        context: ClassificationContext,
        mode: EngineMode,
    ): ClassificationResult {
        val rulesResult = rulesClassifier.classify(input, context)

        return when (mode) {
            EngineMode.OnDeviceOnly -> onDeviceClassifier?.classify(input, context) ?: rulesResult
            EngineMode.ApiOnly -> apiClassifier?.classify(input, context) ?: rulesResult
            EngineMode.Auto -> autoClassify(input, context, rulesResult)
        }
    }

    private suspend fun autoClassify(
        input: IngredientInput,
        context: ClassificationContext,
        rulesResult: ClassificationResult,
    ): ClassificationResult {
        if (rulesResult.confidence >= 0.85f) {
            return rulesResult
        }

        if (context.preferOnDevice) {
            onDeviceClassifier?.let { return it.classify(input, context) }
        }

        if (context.allowNetwork && context.apiFallbackEnabled) {
            apiClassifier?.let { return it.classify(input, context) }
        }

        return rulesResult
    }
}
