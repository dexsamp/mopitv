package com.example.data.api

import android.content.Context
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.File
import java.util.concurrent.TimeUnit

// --- DATA MODELS FOR STENLY OTAKUDESU API V1 ---
// Endpoints: https://stenly.co/stenlyapi-hub/api/v1/otakudesu

data class StenlyAnimeCard(
    @field:Json(name = "title") val title: String? = null,
    @field:Json(name = "url") val url: String? = null,
    @field:Json(name = "slug") val slug: String? = null,
    @field:Json(name = "thumbnail") val thumbnail: String? = null,
    @field:Json(name = "poster") val poster: String? = null,
    @field:Json(name = "thumb") val thumb: String? = null,
    @field:Json(name = "episode") val episode: String? = null,
    @field:Json(name = "episodes") val episodes: String? = null,
    @field:Json(name = "dayOrRating") val dayOrRating: String? = null,
    @field:Json(name = "rating") val rating: String? = null,
    @field:Json(name = "studio") val studio: String? = null,
    @field:Json(name = "genres") val genres: List<String>? = null,
    @field:Json(name = "date") val date: String? = null,
    @field:Json(name = "status") val status: String? = null
) {
    val cleanSlug: String
        get() = (slug ?: url ?: "").removePrefix("https://otakudesu.blog/anime/").removePrefix("/anime/").removePrefix("/").removeSuffix("/")

    val displayPoster: String
        get() = thumbnail ?: poster ?: thumb ?: "https://cdn.myanimelist.net/images/anime/1170/124312l.jpg"
}

data class StenlyHomeResult(
    @field:Json(name = "ongoing") val ongoing: List<StenlyAnimeCard>? = null,
    @field:Json(name = "complete") val complete: List<StenlyAnimeCard>? = null
)

data class StenlyHomeResponse(
    @field:Json(name = "Status") val status: Boolean? = null,
    @field:Json(name = "Code") val code: Int? = null,
    @field:Json(name = "TotalOngoing") val totalOngoing: Int? = null,
    @field:Json(name = "TotalComplete") val totalComplete: Int? = null,
    @field:Json(name = "Result") val result: StenlyHomeResult? = null
)

data class StenlyEpisodeItem(
    @field:Json(name = "title") val title: String? = null,
    @field:Json(name = "url") val url: String? = null,
    @field:Json(name = "slug") val slug: String? = null,
    @field:Json(name = "date") val date: String? = null
) {
    val cleanSlug: String
        get() = (slug ?: url ?: "").removePrefix("https://otakudesu.blog/episode/").removePrefix("/episode/").removePrefix("/").removeSuffix("/")
}

data class StenlyDetailResult(
    @field:Json(name = "title") val title: String? = null,
    @field:Json(name = "japanese") val japanese: String? = null,
    @field:Json(name = "score") val score: String? = null,
    @field:Json(name = "producer") val producer: String? = null,
    @field:Json(name = "type") val type: String? = null,
    @field:Json(name = "status") val status: String? = null,
    @field:Json(name = "totalEpisodes") val totalEpisodes: String? = null,
    @field:Json(name = "duration") val duration: String? = null,
    @field:Json(name = "releaseDate") val releaseDate: String? = null,
    @field:Json(name = "studio") val studio: String? = null,
    @field:Json(name = "genres") val genres: List<String>? = null,
    @field:Json(name = "synopsis") val synopsis: String? = null,
    @field:Json(name = "thumbnail") val thumbnail: String? = null,
    @field:Json(name = "url") val url: String? = null,
    @field:Json(name = "slug") val slug: String? = null,
    @field:Json(name = "batch") val batch: String? = null,
    @field:Json(name = "episodes") val episodes: List<StenlyEpisodeItem>? = null
)

data class StenlyDetailResponse(
    @field:Json(name = "Status") val status: Boolean? = null,
    @field:Json(name = "Code") val code: Int? = null,
    @field:Json(name = "Result") val result: StenlyDetailResult? = null
)

data class StenlyStream(
    @field:Json(name = "server") val server: String? = null,
    @field:Json(name = "quality") val quality: String? = null,
    @field:Json(name = "type") val type: String? = null,
    @field:Json(name = "url") val url: String? = null,
    @field:Json(name = "raw_url") val rawUrl: String? = null,
    @field:Json(name = "proxy_url") val proxyUrl: String? = null,
    @field:Json(name = "stream_url") val streamUrl: String? = null,
    @field:Json(name = "isDirect") val isDirect: Boolean? = null
)

data class StenlyWatchResult(
    @field:Json(name = "title") val title: String? = null,
    @field:Json(name = "episodeNumber") val episodeNumber: Any? = null,
    @field:Json(name = "animeTitle") val animeTitle: String? = null,
    @field:Json(name = "animeSlug") val animeSlug: String? = null,
    @field:Json(name = "url") val url: String? = null,
    @field:Json(name = "slug") val slug: String? = null,
    @field:Json(name = "defaultEmbed") val defaultEmbed: String? = null,
    @field:Json(name = "streams") val streams: List<StenlyStream>? = null
) {
    /**
     * Resolves the most reliable and direct playable stream link:
     * 1. MP4 direct proxy url (streamable directly in HTML5 video tag, verified 200 OK)
     * 2. HLS stream proxy url (m3u8 format with Hls.js fallback)
     * 3. Any proxy url or stream url
     * 4. defaultEmbed (Blogger/DesuStream responsive embed iframe)
     * 5. Raw direct stream URL
     */
    val bestVideoUrl: String?
        get() {
            // 1. Direct MP4 proxy (e.g. ondesuhd 720p videoplayback streamable directly)
            val mp4Proxy = streams?.firstOrNull { it.type == "mp4" && !it.proxyUrl.isNullOrBlank() }?.proxyUrl
            if (!mp4Proxy.isNullOrBlank()) return mp4Proxy

            // 2. Official OtakuDesu responsive embed player (DesuStream/Blogger iframe) - always functional
            if (!defaultEmbed.isNullOrBlank()) return defaultEmbed

            // 3. Fallback to any available proxy or stream URL
            val anyProxy = streams?.firstOrNull { !it.proxyUrl.isNullOrBlank() }?.proxyUrl
            if (!anyProxy.isNullOrBlank()) return anyProxy

            val anyStream = streams?.firstOrNull { !it.streamUrl.isNullOrBlank() }?.streamUrl
            if (!anyStream.isNullOrBlank()) return anyStream

            val raw = streams?.firstOrNull { !it.rawUrl.isNullOrBlank() }?.rawUrl
            if (!raw.isNullOrBlank()) return raw

            return streams?.firstOrNull { !it.url.isNullOrBlank() }?.url
        }
}

data class StenlyWatchResponse(
    @field:Json(name = "Status") val status: Boolean? = null,
    @field:Json(name = "Code") val code: Int? = null,
    @field:Json(name = "Result") val result: StenlyWatchResult? = null
)

data class StenlyScheduleAnime(
    @field:Json(name = "title") val title: String? = null,
    @field:Json(name = "url") val url: String? = null,
    @field:Json(name = "slug") val slug: String? = null
)

data class StenlyScheduleDay(
    @field:Json(name = "day") val day: String? = null,
    @field:Json(name = "anime") val anime: List<StenlyScheduleAnime>? = null
)

data class StenlyScheduleResponse(
    @field:Json(name = "Status") val status: Boolean? = null,
    @field:Json(name = "Code") val code: Int? = null,
    @field:Json(name = "TotalDays") val totalDays: Int? = null,
    @field:Json(name = "Result") val result: List<StenlyScheduleDay>? = null
)

data class StenlyGenreItem(
    @field:Json(name = "name") val name: String? = null,
    @field:Json(name = "slug") val slug: String? = null,
    @field:Json(name = "url") val url: String? = null
) {
    val cleanSlug: String
        get() = (slug ?: name ?: "").lowercase().trim()
}

data class StenlyGenresListResponse(
    @field:Json(name = "Status") val status: Boolean? = null,
    @field:Json(name = "Code") val code: Int? = null,
    @field:Json(name = "Total") val total: Int? = null,
    @field:Json(name = "Result") val result: List<StenlyGenreItem>? = null
)

data class StenlyGenreAnimeResponse(
    @field:Json(name = "Status") val status: Boolean? = null,
    @field:Json(name = "Code") val code: Int? = null,
    @field:Json(name = "Genre") val genre: String? = null,
    @field:Json(name = "Page") val page: Int? = null,
    @field:Json(name = "Total") val total: Int? = null,
    @field:Json(name = "Result") val result: List<StenlyAnimeCard>? = null
)

data class StenlyBatchLink(
    @field:Json(name = "server") val server: String? = null,
    @field:Json(name = "url") val url: String? = null
)

data class StenlyBatchDownload(
    @field:Json(name = "format") val format: String? = null,
    @field:Json(name = "quality") val quality: String? = null,
    @field:Json(name = "size") val size: String? = null,
    @field:Json(name = "links") val links: List<StenlyBatchLink>? = null
)

data class StenlyBatchResult(
    @field:Json(name = "title") val title: String? = null,
    @field:Json(name = "url") val url: String? = null,
    @field:Json(name = "slug") val slug: String? = null,
    @field:Json(name = "downloads") val downloads: List<StenlyBatchDownload>? = null
)

data class StenlyBatchResponse(
    @field:Json(name = "Status") val status: Boolean? = null,
    @field:Json(name = "Code") val code: Int? = null,
    @field:Json(name = "Result") val result: StenlyBatchResult? = null
)

data class StenlySearchResponse(
    @field:Json(name = "Status") val status: Boolean? = null,
    @field:Json(name = "Code") val code: Int? = null,
    @field:Json(name = "Query") val query: String? = null,
    @field:Json(name = "Total") val total: Int? = null,
    @field:Json(name = "Result") val result: List<StenlyAnimeCard>? = null
)

// --- RETROFIT SERVICE INTERFACE ---

/**
 * Official client for Stenly API Hub Otakudesu:
 * - Home: https://stenly.co/stenlyapi-hub/api/v1/otakudesu
 * - Detail: https://stenly.co/stenlyapi-hub/api/v1/otakudesu/detail?slug=...
 * - Watch: https://stenly.co/stenlyapi-hub/api/v1/otakudesu/watch?slug=...
 * - Schedule: https://stenly.co/stenlyapi-hub/api/v1/otakudesu?type=schedule
 * - Genres: https://stenly.co/stenlyapi-hub/api/v1/otakudesu?type=genres
 * - Genre Filter: https://stenly.co/stenlyapi-hub/api/v1/otakudesu?genre=action&page=1
 * - Batch: https://stenly.co/stenlyapi-hub/api/v1/otakudesu?batch=...
 * - Search: https://stenly.co/stenlyapi-hub/api/v1/otakudesu?q=...
 */
interface StenlyOtakudesuApi {

    @GET("https://stenly.co/stenlyapi-hub/api/v1/otakudesu")
    suspend fun getHome(): StenlyHomeResponse

    @GET("https://stenly.co/stenlyapi-hub/api/v1/otakudesu/detail")
    suspend fun getAnimeDetail(@Query("slug") slug: String): StenlyDetailResponse

    @GET("https://stenly.co/stenlyapi-hub/api/v1/otakudesu/watch")
    suspend fun getWatchEpisode(@Query("slug") episodeSlug: String): StenlyWatchResponse

    @GET("https://stenly.co/stenlyapi-hub/api/v1/otakudesu")
    suspend fun getSchedule(@Query("type") type: String = "schedule"): StenlyScheduleResponse

    @GET("https://stenly.co/stenlyapi-hub/api/v1/otakudesu")
    suspend fun getGenresList(@Query("type") type: String = "genres"): StenlyGenresListResponse

    @GET("https://stenly.co/stenlyapi-hub/api/v1/otakudesu")
    suspend fun getAnimeByGenre(
        @Query("genre") genre: String,
        @Query("page") page: Int = 1
    ): StenlyGenreAnimeResponse

    @GET("https://stenly.co/stenlyapi-hub/api/v1/otakudesu")
    suspend fun getBatch(@Query("batch") batchSlug: String): StenlyBatchResponse

    @GET("https://stenly.co/stenlyapi-hub/api/v1/otakudesu")
    suspend fun searchAnime(@Query("q") query: String): StenlySearchResponse

    companion object {
        const val BASE_URL = "https://stenly.co/stenlyapi-hub/api/v1/"

        fun create(context: Context? = null, baseUrl: String = BASE_URL): StenlyOtakudesuApi {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            val clientBuilder = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val original = chain.request()
                    val request = original.newBuilder()
                        .header("User-Agent", "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36 MoppiTV/1.0")
                        .header("Accept", "application/json, text/plain, */*")
                        .header("Referer", "https://otakudesu.blog/")
                        .build()
                    chain.proceed(request)
                }
                .addInterceptor(logging)
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)

            if (context != null) {
                val cacheDir = File(context.cacheDir, "stenly_otaku_v1_cache")
                clientBuilder.cache(Cache(cacheDir, 25L * 1024 * 1024))
            }

            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            return Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(clientBuilder.build())
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(StenlyOtakudesuApi::class.java)
        }
    }
}
