package com.example.data.api

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.io.File
import java.util.concurrent.TimeUnit

data class JikanImageSet(
    @field:Json(name = "image_url") val imageUrl: String? = null,
    @field:Json(name = "small_image_url") val smallImageUrl: String? = null,
    @field:Json(name = "large_image_url") val largeImageUrl: String? = null
)

data class JikanImages(
    @field:Json(name = "jpg") val jpg: JikanImageSet? = null,
    @field:Json(name = "webp") val webp: JikanImageSet? = null
)

data class JikanTrailerImages(
    @field:Json(name = "image_url") val imageUrl: String? = null,
    @field:Json(name = "small_image_url") val smallImageUrl: String? = null,
    @field:Json(name = "medium_image_url") val mediumImageUrl: String? = null,
    @field:Json(name = "large_image_url") val largeImageUrl: String? = null,
    @field:Json(name = "maximum_image_url") val maximumImageUrl: String? = null
)

data class JikanTrailer(
    @field:Json(name = "youtube_id") val youtubeId: String? = null,
    @field:Json(name = "url") val url: String? = null,
    @field:Json(name = "embed_url") val embedUrl: String? = null,
    @field:Json(name = "images") val images: JikanTrailerImages? = null
)

data class JikanGenre(
    @field:Json(name = "mal_id") val malId: Int? = null,
    @field:Json(name = "name") val name: String? = null
)

data class JikanStudio(
    @field:Json(name = "mal_id") val malId: Int? = null,
    @field:Json(name = "name") val name: String? = null
)

data class JikanAnime(
    @field:Json(name = "mal_id") val malId: Int? = null,
    @field:Json(name = "title") val title: String? = null,
    @field:Json(name = "title_english") val titleEnglish: String? = null,
    @field:Json(name = "title_japanese") val titleJapanese: String? = null,
    @field:Json(name = "synopsis") val synopsis: String? = null,
    @field:Json(name = "images") val images: JikanImages? = null,
    @field:Json(name = "trailer") val trailer: JikanTrailer? = null,
    @field:Json(name = "episodes") val episodes: Int? = null,
    @field:Json(name = "status") val status: String? = null,
    @field:Json(name = "score") val score: Double? = null,
    @field:Json(name = "rank") val rank: Int? = null,
    @field:Json(name = "season") val season: String? = null,
    @field:Json(name = "year") val year: Int? = null,
    @field:Json(name = "genres") val genres: List<JikanGenre>? = null,
    @field:Json(name = "studios") val studios: List<JikanStudio>? = null
)

data class JikanTopResponse(
    @field:Json(name = "data") val data: List<JikanAnime>? = null
)

data class JikanSeasonResponse(
    @field:Json(name = "data") val data: List<JikanAnime>? = null
)

data class JikanPromoVideo(
    @field:Json(name = "title") val title: String? = null,
    @field:Json(name = "trailer") val trailer: JikanTrailer? = null
)

data class JikanEpisodeVideoItem(
    @field:Json(name = "mal_id") val malId: Int? = null,
    @field:Json(name = "title") val title: String? = null,
    @field:Json(name = "episode") val episode: String? = null,
    @field:Json(name = "url") val url: String? = null,
    @field:Json(name = "images") val images: JikanImages? = null
)

data class JikanVideosData(
    @field:Json(name = "promo") val promo: List<JikanPromoVideo>? = null,
    @field:Json(name = "episodes") val episodes: List<JikanEpisodeVideoItem>? = null
)

data class JikanVideosResponse(
    @field:Json(name = "data") val data: JikanVideosData? = null
)

data class JikanEpisodeItem(
    @field:Json(name = "mal_id") val malId: Int? = null,
    @field:Json(name = "title") val title: String? = null,
    @field:Json(name = "title_japanese") val titleJapanese: String? = null,
    @field:Json(name = "score") val score: Double? = null,
    @field:Json(name = "filler") val filler: Boolean? = null,
    @field:Json(name = "recap") val recap: Boolean? = null
)

data class JikanEpisodesResponse(
    @field:Json(name = "data") val data: List<JikanEpisodeItem>? = null
)

/**
 * Real Anime Scraper & Metadata API client using Jikan v4 (Official Open MAL Scraper).
 * Strictly implements Jikan Cache Validation (ETag & Last-Modified) according to:
 * https://docs.api.jikan.moe/#/section/information/cache-validation
 * Server: https://api.jikan.moe/v4/
 */
interface AnimeScraperApi {

    @GET("top/anime")
    suspend fun getTopAnime(
        @Query("filter") filter: String = "bypopularity",
        @Query("limit") limit: Int = 25,
        @Query("page") page: Int = 1
    ): JikanTopResponse

    @GET("seasons/now")
    suspend fun getCurrentSeason(
        @Query("limit") limit: Int = 25,
        @Query("page") page: Int = 1
    ): JikanSeasonResponse

    @GET("anime")
    suspend fun searchAnime(
        @Query("q") query: String,
        @Query("limit") limit: Int = 20
    ): JikanTopResponse

    @GET("anime/{id}/videos")
    suspend fun getAnimeVideos(
        @Path("id") animeId: Int
    ): JikanVideosResponse

    @GET("anime/{id}/episodes")
    suspend fun getAnimeEpisodes(
        @Path("id") animeId: Int,
        @Query("page") page: Int = 1
    ): JikanEpisodesResponse

    companion object {
        private const val BASE_URL = "https://api.jikan.moe/v4/"
        private const val CACHE_SIZE = 30L * 1024 * 1024 // 30 MB Cache for responsive performance

        fun create(context: Context? = null): AnimeScraperApi {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            val clientBuilder = OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)

            // Implement Cache Validation (ETag & Last-Modified)
            if (context != null) {
                val cacheDir = File(context.cacheDir, "jikan_v4_cache")
                val cache = Cache(cacheDir, CACHE_SIZE)
                clientBuilder.cache(cache)

                // Cache validation & Offline resilience interceptor
                clientBuilder.addInterceptor(object : Interceptor {
                    override fun intercept(chain: Interceptor.Chain): Response {
                        var request = chain.request()
                        val isOnline = isNetworkAvailable(context)

                        val cacheControl = if (isOnline) {
                            // Online: revalidate cache if stale (ETag / Last-Modified -> 304 Not Modified)
                            CacheControl.Builder()
                                .maxAge(30, TimeUnit.MINUTES)
                                .build()
                        } else {
                            // Offline / Weak network: serve stale cache up to 7 days
                            CacheControl.Builder()
                                .onlyIfCached()
                                .maxStale(7, TimeUnit.DAYS)
                                .build()
                        }

                        request = request.newBuilder()
                            .cacheControl(cacheControl)
                            .header("Accept", "application/json")
                            .header("User-Agent", "MoppiTV-Android/1.0 (Jikan v4 CacheValidation Client)")
                            .build()

                        val response = try {
                            chain.proceed(request)
                        } catch (e: Exception) {
                            // If network failed, attempt to read from offline cache
                            val offlineRequest = request.newBuilder()
                                .cacheControl(CacheControl.FORCE_CACHE)
                                .build()
                            chain.proceed(offlineRequest)
                        }

                        // Rate limit handling (Jikan v4 HTTP 429)
                        if (response.code == 429) {
                            // If rate limited, try serving from cache
                            val cachedRequest = request.newBuilder()
                                .cacheControl(CacheControl.FORCE_CACHE)
                                .build()
                            val cachedResponse = chain.proceed(cachedRequest)
                            if (cachedResponse.isSuccessful) {
                                return cachedResponse
                            }
                        }

                        return response
                    }
                })
            }

            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(clientBuilder.build())
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(AnimeScraperApi::class.java)
        }

        @Suppress("DEPRECATION")
        private fun isNetworkAvailable(context: Context): Boolean {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
            return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                val network = cm.activeNetwork ?: return false
                val caps = cm.getNetworkCapabilities(network) ?: return false
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            } else {
                val activeInfo = cm.activeNetworkInfo
                activeInfo != null && activeInfo.isConnected
            }
        }
    }
}
