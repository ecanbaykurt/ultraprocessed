package com.b2.ultraprocessed.classify

class ApiLLMClassifier(
    private val gateway: RemoteClassifierGateway,
) : Classifier {
    override val id: String = "api_llm"

    override suspend fun classify(
        input: IngredientInput,
        context: ClassificationContext,
    ): ClassificationResult {
        require(context.allowNetwork) {
            "API classification requested without network permission."
        }

        return gateway.classify(input)
    }
}

fun interface RemoteClassifierGateway {
    suspend fun classify(input: IngredientInput): ClassificationResult
}
