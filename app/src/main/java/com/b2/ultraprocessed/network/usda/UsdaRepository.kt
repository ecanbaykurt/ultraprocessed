package com.b2.ultraprocessed.network.usda

class UsdaRepository(
    private val dataSource: UsdaApiDataSource,
) {
    suspend fun lookupByBarcode(upc: String): UsdaFoodRecord? {
        val normalizedUpc = normalizeUpc(upc)
        if (normalizedUpc.isBlank()) return null

        val candidates = dataSource.searchFoods(query = normalizedUpc, pageSize = 25)
        if (candidates.isEmpty()) return null
        val picked = rankBarcodeCandidates(candidates, normalizedUpc) ?: return null
        val detail = dataSource.fetchFoodDetail(picked.fdcId)
        return toRecord(detail ?: picked.toDetailFallback())
    }

    suspend fun lookupByQuery(query: String): UsdaFoodRecord? {
        if (query.isBlank()) return null
        val candidates = dataSource.searchFoods(query = query, pageSize = 10)
        val picked = candidates.firstOrNull() ?: return null
        val detail = dataSource.fetchFoodDetail(picked.fdcId)
        return toRecord(detail ?: picked.toDetailFallback())
    }

    private fun rankBarcodeCandidates(
        candidates: List<UsdaSearchFood>,
        normalizedUpc: String,
    ): UsdaSearchFood? {
        val exact = candidates.firstOrNull { normalizeUpc(it.gtinUpc).matchesUpc(normalizedUpc) }
        if (exact != null) return exact

        val branded = candidates.firstOrNull { it.dataType.equals("Branded", ignoreCase = true) }
        if (branded != null) return branded
        return candidates.firstOrNull()
    }

    private fun toRecord(detail: UsdaFoodDetail): UsdaFoodRecord = UsdaFoodRecord(
        fdcId = detail.fdcId,
        productName = detail.description.ifBlank { "USDA product" },
        brandOwner = detail.brandOwner,
        gtinUpc = detail.gtinUpc,
        ingredientsText = detail.ingredients,
    )
}

private fun UsdaSearchFood.toDetailFallback(): UsdaFoodDetail = UsdaFoodDetail(
    fdcId = fdcId,
    description = description,
    brandOwner = brandOwner,
    gtinUpc = gtinUpc,
    ingredients = ingredients,
)

private fun normalizeUpc(value: String?): String =
    value.orEmpty().filter(Char::isDigit).trimStart('0')

private fun String?.matchesUpc(normalizedTarget: String): Boolean {
    val mine = normalizeUpc(this)
    return mine == normalizedTarget || mine.endsWith(normalizedTarget) || normalizedTarget.endsWith(mine)
}
