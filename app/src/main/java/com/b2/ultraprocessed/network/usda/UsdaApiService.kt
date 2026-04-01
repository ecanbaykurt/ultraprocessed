package com.b2.ultraprocessed.network.usda

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request

class UsdaApiService(
    private val apiKeyProvider: UsdaApiKeyProvider,
    private val client: OkHttpClient = OkHttpClient(),
) : UsdaApiDataSource {
    override suspend fun searchFoods(
        query: String,
        pageSize: Int,
    ): List<UsdaSearchFood> = withContext(Dispatchers.IO) {
        val apiKey = apiKeyProvider.getApiKey()
        if (apiKey.isBlank()) return@withContext emptyList()

        val url = "$BASE_URL/foods/search".toHttpUrl().newBuilder()
            .addQueryParameter("api_key", apiKey)
            .addQueryParameter("query", query)
            .addQueryParameter("pageSize", pageSize.toString())
            .addQueryParameter("dataType", "Branded")
            .build()
        val request = Request.Builder().url(url).get().build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return@withContext emptyList()
            val body = response.body?.string() ?: return@withContext emptyList()
            UsdaJsonParser.parseSearchFoods(body)
        }
    }

    override suspend fun fetchFoodDetail(fdcId: Long): UsdaFoodDetail? = withContext(Dispatchers.IO) {
        val apiKey = apiKeyProvider.getApiKey()
        if (apiKey.isBlank()) return@withContext null

        val url = "$BASE_URL/food/$fdcId".toHttpUrl().newBuilder()
            .addQueryParameter("api_key", apiKey)
            .build()
        val request = Request.Builder().url(url).get().build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return@withContext null
            val body = response.body?.string() ?: return@withContext null
            UsdaJsonParser.parseFoodDetail(body)
        }
    }

    companion object {
        const val BASE_URL: String = "https://api.nal.usda.gov/fdc/v1"
    }
}
