package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.example.ui.theme.MoppiCyan
import kotlinx.coroutines.delay

/**
 * Resilient, high-performance Media Player for Anime Streaming.
 * Exclusively plays OtakuDesu video streams (videoplayback / mp4 proxy / DesuStream embed / HLS):
 * - Never falls back to YouTube for anime playback.
 * - Hardware accelerated HTML5 & responsive embed players.
 * - Clean buffering handling with automatic recovery.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun RealAnimeVideoPlayer(
    videoUrl: String,
    thumbnailUrl: String? = null,
    isPlaying: Boolean = true,
    isLooping: Boolean = false,
    isMuted: Boolean = false,
    seekToSeconds: Int? = null,
    onPlaybackProgress: (currentSec: Int, durationSec: Int) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isBuffering by remember { mutableStateOf(true) }
    var isNetworkSlowOrOffline by remember { mutableStateOf(false) }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var lastLoadedUrl by remember { mutableStateOf("") }
    val activeUrl = videoUrl
    var bufferTimeout by remember(activeUrl) { mutableStateOf(false) }

    // Auto-clear buffering spinner after stream starts (hard maximum 2 seconds)
    LaunchedEffect(activeUrl) {
        bufferTimeout = false
        if (activeUrl.isNotBlank()) {
            isBuffering = true
            delay(2000)
            isBuffering = false
            bufferTimeout = true
        } else {
            isBuffering = false
        }
    }

    // Network connectivity monitoring for graceful recovery
    DisposableEffect(context) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                isNetworkSlowOrOffline = false
            }

            override fun onLost(network: Network) {
                isNetworkSlowOrOffline = true
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        try {
            cm?.registerNetworkCallback(request, networkCallback)
        } catch (e: Exception) {
            // ignore permission if restricted in test environment
        }

        onDispose {
            try {
                cm?.unregisterNetworkCallback(networkCallback)
            } catch (e: Exception) {
                // ignore
            }
            try {
                webViewInstance?.stopLoading()
                webViewInstance?.onPause()
                webViewInstance?.destroy()
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    val isYouTube = remember(activeUrl) {
        activeUrl.contains("youtube.com") || activeUrl.contains("youtu.be") || activeUrl.contains("youtube-nocookie.com")
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("real_anime_video_player")
    ) {
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setLayerType(View.LAYER_TYPE_HARDWARE, null)

                    settings.apply {
                        javaScriptEnabled = true
                        mediaPlaybackRequiresUserGesture = false
                        domStorageEnabled = true
                        databaseEnabled = true
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        allowFileAccess = true
                        allowContentAccess = true
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        userAgentString = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                    }

                    // JavaScript Bridge to communicate playback events to Compose
                    addJavascriptInterface(object {
                        @JavascriptInterface
                        fun onVideoReady() {
                            post {
                                isBuffering = false
                            }
                        }

                        @JavascriptInterface
                        fun onVideoPlaying() {
                            post {
                                isBuffering = false
                            }
                        }

                        @JavascriptInterface
                        fun onVideoBuffering() {
                            post {
                                isBuffering = true
                            }
                        }

                        @JavascriptInterface
                        fun onVideoProgress(curr: Int, total: Int) {
                            post {
                                onPlaybackProgress(curr, total)
                            }
                        }

                        @JavascriptInterface
                        fun onVideoError(code: Int) {
                            post {
                                isBuffering = false
                            }
                        }
                    }, "AndroidBridge")

                    webChromeClient = WebChromeClient()
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            isBuffering = false
                        }

                        private fun isAllowedStreamUrl(reqUrl: String): Boolean {
                            return reqUrl.contains("otakudesu") || reqUrl.contains("desustream") || reqUrl.contains("blogger.com") || reqUrl.contains("googlevideo.com") || reqUrl.contains("stenly.co") || reqUrl.contains("youtube.com")
                        }

                        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                            val reqUrl = request?.url?.toString() ?: return false
                            return !isAllowedStreamUrl(reqUrl)
                        }

                        @Deprecated("Deprecated in Java")
                        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                            val reqUrl = url ?: return false
                            return !isAllowedStreamUrl(reqUrl)
                        }

                        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                            // Suppress harmless subresource errors
                        }
                    }

                    if (activeUrl.isNotBlank()) {
                        loadPlayerContent(this, activeUrl, isPlaying, isLooping, isMuted, thumbnailUrl)
                        lastLoadedUrl = activeUrl
                    }
                    webViewInstance = this
                }
            },
            update = { wv ->
                webViewInstance = wv
                // Re-initialize player immediately if videoUrl changed
                if (activeUrl.isNotBlank() && activeUrl != lastLoadedUrl) {
                    lastLoadedUrl = activeUrl
                    loadPlayerContent(wv, activeUrl, isPlaying, isLooping, isMuted, thumbnailUrl)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Reactive Play / Pause control without page reload
        LaunchedEffect(isPlaying) {
            webViewInstance?.evaluateJavascript(
                if (isYouTube) {
                    """
                    try {
                        if (window.player && player.playVideo && ${isPlaying}) {
                            player.playVideo();
                        } else if (window.player && player.pauseVideo && !${isPlaying}) {
                            player.pauseVideo();
                        }
                    } catch(e) {}
                    """.trimIndent()
                } else {
                    """
                    try {
                        const v = document.getElementById('mp4player');
                        if (v) {
                            if (${isPlaying}) { v.play().catch(function(){}); } else { v.pause(); }
                        }
                    } catch(e) {}
                    """.trimIndent()
                },
                null
            )
        }

        // Reactive Seek Controller without page reload
        LaunchedEffect(seekToSeconds) {
            seekToSeconds?.let { sec ->
                webViewInstance?.evaluateJavascript(
                    if (isYouTube) {
                        """
                        try {
                            if (window.player && player.seekTo) {
                                player.seekTo($sec, true);
                            }
                        } catch(e) {}
                        """.trimIndent()
                    } else {
                        """
                        try {
                            const v = document.getElementById('mp4player');
                            if (v && Math.abs(v.currentTime - $sec) > 2) {
                                v.currentTime = $sec;
                            }
                        } catch(e) {}
                        """.trimIndent()
                    },
                    null
                )
            }
        }

        // Reactive Mute Controller
        LaunchedEffect(isMuted) {
            webViewInstance?.evaluateJavascript(
                if (isYouTube) {
                    """
                    try {
                        if (window.player) {
                            if (${isMuted}) { player.mute(); } else { player.unMute(); }
                        }
                    } catch(e) {}
                    """.trimIndent()
                } else {
                    """
                    try {
                        const v = document.getElementById('mp4player');
                        if (v) { v.muted = ${isMuted}; }
                    } catch(e) {}
                    """.trimIndent()
                },
                null
            )
        }

        // Slow Network Banner (Non-intrusive top indicator)
        if (isNetworkSlowOrOffline) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.Black.copy(alpha = 0.85f),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.WifiOff,
                        contentDescription = "Slow Network",
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Koneksi lambat • Mengoptimalkan cache...",
                        color = Color.White,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Loading / Buffering Spinner Overlay (Auto-times out so it never spins infinitely)
        AnimatedVisibility(
            visible = (isBuffering && !bufferTimeout) || (activeUrl.isBlank() && !bufferTimeout),
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.65f)),
                contentAlignment = Alignment.Center
            ) {
                if (!thumbnailUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = thumbnailUrl,
                        contentDescription = "Buffering Preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.Black.copy(alpha = 0.88f),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = MoppiCyan,
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Menyiapkan Stream OtakuDesu HD...",
                                color = Color.White,
                                fontSize = 12.sp,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Mengambil stream dari server OtakuDesu",
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Loads the HTML5 video or OtakuDesu embed player into the WebView.
 */
private fun loadPlayerContent(
    webView: WebView,
    url: String,
    isPlaying: Boolean,
    isLooping: Boolean,
    isMuted: Boolean,
    thumbnailUrl: String?
) {
    if (url.isBlank()) return

    val isYouTube = url.contains("youtube.com") || url.contains("youtu.be") || url.contains("youtube-nocookie.com")
    val isEmbed = !isYouTube && (url.contains("blogger.com") || url.contains("desustream.net") || url.contains("link.desustream.com") || url.contains("/dstream/") || url.contains("/embed/"))
    val isHls = !isYouTube && !isEmbed && (url.contains(".m3u8") || url.contains("acek-cdn") || url.contains("dramiyos-cdn"))

    val playerHtml = if (isYouTube) {
        val videoId = extractYouTubeId(url)
        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <style>
                * { margin:0; padding:0; box-sizing:border-box; }
                html, body { width:100%; height:100%; background:#000; overflow:hidden; display:flex; align-items:center; justify-content:center; }
                iframe { width:100%; height:100%; border:none; }
            </style>
        </head>
        <body>
            <iframe id="ytplayer" 
                src="https://www.youtube.com/embed/$videoId?enablejsapi=1&autoplay=${if (isPlaying) 1 else 0}&playsinline=1&controls=1&rel=0&modestbranding=1&loop=${if (isLooping) 1 else 0}&playlist=$videoId&origin=https://www.youtube.com" 
                allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" 
                allowfullscreen>
            </iframe>
        </body>
        </html>
        """.trimIndent()
    } else if (isEmbed) {
        // OtakuDesu / DesuStream / Blogger Embed Player
        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <style>
                * { margin:0; padding:0; box-sizing:border-box; }
                html, body { width:100%; height:100%; background:#000; overflow:hidden; }
                iframe { width:100%; height:100%; border:none; }
            </style>
        </head>
        <body>
            <iframe id="embedplayer" 
                src="$url" 
                allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" 
                allowfullscreen>
            </iframe>
            <script>
                function doneLoading() {
                    if (window.AndroidBridge) window.AndroidBridge.onVideoReady();
                }
                window.addEventListener('load', doneLoading);
                setTimeout(doneLoading, 1500);
            </script>
        </body>
        </html>
        """.trimIndent()
    } else if (isHls) {
        // HLS Stream Player
        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <script src="https://cdn.jsdelivr.net/npm/hls.js@1.5.8/dist/hls.min.js"></script>
            <style>
                * { margin:0; padding:0; box-sizing:border-box; }
                html, body { width:100%; height:100%; background:#000; overflow:hidden; }
                video { width:100%; height:100%; object-fit:contain; background:#000; }
            </style>
        </head>
        <body>
            <video id="mp4player" 
                poster="${thumbnailUrl ?: ""}" 
                ${if (isPlaying) "autoplay" else ""} 
                ${if (isLooping) "loop" else ""} 
                ${if (isMuted) "muted" else ""} 
                playsinline controls preload="auto">
            </video>
            <script>
                var v = document.getElementById('mp4player');
                var src = "$url";

                function setupHls() {
                    if (window.Hls && Hls.isSupported()) {
                        var hls = new Hls({ enableWorker: true });
                        hls.loadSource(src);
                        hls.attachMedia(v);
                        hls.on(Hls.Events.MANIFEST_PARSED, function() {
                            if (window.AndroidBridge) window.AndroidBridge.onVideoReady();
                            ${if (isPlaying) "v.play().catch(function(){});" else ""}
                        });
                        hls.on(Hls.Events.ERROR, function(event, data) {
                            if (data.fatal && data.type === Hls.ErrorTypes.NETWORK_ERROR) {
                                hls.startLoad();
                            }
                        });
                    } else if (v.canPlayType('application/vnd.apple.mpegurl')) {
                        v.src = src;
                        if (window.AndroidBridge) window.AndroidBridge.onVideoReady();
                        ${if (isPlaying) "v.play().catch(function(){});" else ""}
                    } else {
                        v.src = src;
                    }
                }

                v.addEventListener('playing', function() {
                    if (window.AndroidBridge) window.AndroidBridge.onVideoPlaying();
                });
                v.addEventListener('timeupdate', function() {
                    if (window.AndroidBridge) {
                        window.AndroidBridge.onVideoProgress(Math.floor(v.currentTime), Math.floor(v.duration || 0));
                    }
                });
                setupHls();
            </script>
        </body>
        </html>
        """.trimIndent()
    } else {
        // Direct stream / videoplayback URL player
        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <style>
                * { margin:0; padding:0; box-sizing:border-box; }
                html, body { width:100%; height:100%; background:#000; overflow:hidden; display:flex; align-items:center; justify-content:center; }
                video { width:100%; height:100%; object-fit:contain; background:#000; }
            </style>
        </head>
        <body>
            <video id="mp4player" 
                src="$url"
                poster="${thumbnailUrl ?: ""}" 
                ${if (isPlaying) "autoplay" else ""} 
                ${if (isLooping) "loop" else ""} 
                ${if (isMuted) "muted" else ""} 
                playsinline 
                controls 
                preload="auto">
                <source src="$url" type="video/mp4">
            </video>
            <script>
                var v = document.getElementById('mp4player');
                function notifyReady() {
                    if (window.AndroidBridge) window.AndroidBridge.onVideoReady();
                }
                function notifyPlaying() {
                    if (window.AndroidBridge) window.AndroidBridge.onVideoPlaying();
                }
                function notifyBuffering() {
                    if (window.AndroidBridge) window.AndroidBridge.onVideoBuffering();
                }

                v.addEventListener('canplay', notifyReady);
                v.addEventListener('canplaythrough', notifyReady);
                v.addEventListener('playing', notifyPlaying);
                v.addEventListener('play', notifyPlaying);
                v.addEventListener('waiting', notifyBuffering);
                v.addEventListener('timeupdate', function() {
                    if (window.AndroidBridge) {
                        window.AndroidBridge.onVideoProgress(Math.floor(v.currentTime), Math.floor(v.duration || 0));
                    }
                });

                ${if (isPlaying) "v.play().catch(function(e){});" else ""}
            </script>
        </body>
        </html>
        """.trimIndent()
    }

    val baseUrl = when {
        isYouTube -> "https://www.youtube.com"
        url.contains("stenly.co") -> "https://stenly.co/"
        else -> "https://otakudesu.blog/"
    }
    webView.loadDataWithBaseURL(baseUrl, playerHtml, "text/html", "UTF-8", baseUrl)
}

/**
 * Extracts YouTube video ID from standard, shortened, or embed YouTube URLs
 */
private fun extractYouTubeId(url: String): String {
    return try {
        if (url.contains("youtu.be/")) {
            url.substringAfter("youtu.be/").substringBefore("?").substringBefore("&")
        } else if (url.contains("v=")) {
            url.substringAfter("v=").substringBefore("&").substringBefore("?")
        } else if (url.contains("/embed/")) {
            url.substringAfter("/embed/").substringBefore("?").substringBefore("&")
        } else {
            url
        }
    } catch (e: Exception) {
        url
    }
}
