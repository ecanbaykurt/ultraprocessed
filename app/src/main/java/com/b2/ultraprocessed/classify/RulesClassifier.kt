package com.b2.ultraprocessed.classify

class RulesClassifier : Classifier {
    override val id: String = "rules"

    override suspend fun classify(
        input: IngredientInput,
        context: ClassificationContext,
    ): ClassificationResult {
        val normalized = input.normalizedText.lowercase()
        val markers = listOf(
            "emulsifier",
            "maltodextrin",
            "artificial flavor",
            "natural flavor",
            "color added",
            "stabilizer",
        ).filter { normalized.contains(it) }

        val novaGroup = if (markers.isNotEmpty()) 4 else 1
        val confidence = if (markers.isNotEmpty()) 0.82f else 0.55f
        val explanation = if (markers.isNotEmpty()) {
            "Rules engine detected additive markers commonly associated with ultra-processed foods."
        } else {
            "Rules engine did not find strong ultra-processing markers. Escalate to richer engines when available."
        }

        return ClassificationResult(
            novaGroup = novaGroup,
            confidence = confidence,
            markers = markers,
            explanation = explanation,
            highlightTerms = markers,
            engine = id,
        )
    }
}
