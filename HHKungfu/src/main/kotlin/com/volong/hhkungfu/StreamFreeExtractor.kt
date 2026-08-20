package com.volong.hhkungfu

import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.USER_AGENT
import com.lagradost.cloudstream3.utils.ExtractorApi
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.ExtractorLinkType
import com.lagradost.cloudstream3.utils.Qualities
import com.lagradost.cloudstream3.utils.newExtractorLink
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL

/**
 * Extractor for the StreamFree embed used by HHKUNGFU.
 *
 * This version deliberately uses the same plain HTTP/Jsoup approach as the
 * provider instead of CloudStream's `app.get()`, so it does not depend on a
 * Requests type that may be absent from the plugin's compile classpath.
 */
class StreamFreeExtractor : ExtractorApi() {
    override val name = "StreamFree"
    override val mainUrl = "https://streamfree.vip"
    override val requiresReferer = true

    override suspend fun getUrl(
        url: String,
        referer: String?,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        val pageUrl = resolveUrl(url, mainUrl) ?: return
        val pageReferer = resolveUrl(referer ?: mainUrl, mainUrl) ?: mainUrl

        val html = fetchHtml(pageUrl, pageReferer) ?: return
        val document = Jsoup.parse(html, pageUrl)
        val candidates = LinkedHashSet<String>()

        fun addCandidate(raw: String) {
            val resolved = resolveUrl(cleanUrl(raw), pageUrl) ?: return
            if (resolved.contains(".m3u8", true) || resolved.contains(".mp4", true)) {
                candidates.add(resolved)
            }
        }

        // Direct URLs in HTML/inline JavaScript.
        Regex(
            """https?://[^\s"'<>\\]+?\.(?:m3u8|mp4)(?:\?[^\s"'<>\\]*)?""",
            RegexOption.IGNORE_CASE
        ).findAll(html).forEach { addCandidate(it.value) }

        // Escaped JSON/JWPlayer/source fields.
        Regex(
            """(?:file|src|source|url|hls|m3u8)\s*[:=]\s*["']([^"']+)["']""",
            RegexOption.IGNORE_CASE
        ).findAll(html).forEach { addCandidate(it.groupValues[1]) }

        // HTML media/source elements.
        document.select("video[src], video source[src], source[src]").forEach { element: Element ->
            addCandidate(element.attr("src"))
        }

        // Common player configuration attributes.
        document.select("[data-src], [data-url], [data-video], [data-file], [data-source]").forEach { element: Element ->
            listOf("data-src", "data-url", "data-video", "data-file", "data-source")
                .forEach { attr ->
                    val value = element.attr(attr)
                    if (value.isNotBlank()) addCandidate(value)
                }
        }

        for (streamUrl in candidates) {
            val quality = qualityFromUrl(streamUrl)
            val type = if (streamUrl.contains(".m3u8", true)) {
                ExtractorLinkType.M3U8
            } else {
                ExtractorLinkType.VIDEO
            }

            callback(
                newExtractorLink(
                    source = name,
                    name = "StreamFree ${if (quality != Qualities.Unknown.value) "${quality}p" else "Stream"}",
                    url = URL(streamUrl),
                    type = type
                ) {
                    this.referer = pageUrl
                    this.quality = quality
                    this.headers = mapOf("User-Agent" to USER_AGENT)
                }
            )
        }
    }

    private fun fetchHtml(url: String, referer: String): String? {
        return runCatching {
            val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 15000
                readTimeout = 20000
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", USER_AGENT)
                setRequestProperty(
                    "Accept",
                    "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"
                )
                setRequestProperty(
                    "Accept-Language",
                    "vi-VN,vi;q=0.9,en-US;q=0.8,en;q=0.7"
                )
                setRequestProperty("Referer", referer)
            }

            try {
                connection.connect()
                connection.inputStream.use { input ->
                    input.readBytes().toString(Charsets.UTF_8)
                }
            } finally {
                connection.disconnect()
            }
        }.getOrNull()
    }

    private fun cleanUrl(value: String): String = value
        .trim()
        .trim('"', '\'', '`')
        .replace("\\/", "/")
        .replace("\\u0026", "&")
        .replace("&amp;", "&")

    private fun resolveUrl(value: String, base: String): String? {
        if (value.isBlank()) return null
        val cleaned = cleanUrl(value)

        if (cleaned.startsWith("javascript:", true) ||
            cleaned.startsWith("data:", true) ||
            cleaned.startsWith("mailto:", true) ||
            cleaned.startsWith("tel:", true)
        ) return null

        return runCatching {
            URI(base).resolve(cleaned).toString()
        }.getOrNull()?.takeIf {
            it.startsWith("http://", true) || it.startsWith("https://", true)
        }
    }

    private fun qualityFromUrl(url: String): Int = when {
        Regex("(?:2160|4k)", RegexOption.IGNORE_CASE).containsMatchIn(url) -> Qualities.P2160.value
        Regex("1440", RegexOption.IGNORE_CASE).containsMatchIn(url) -> Qualities.P1440.value
        Regex("1080", RegexOption.IGNORE_CASE).containsMatchIn(url) -> Qualities.P1080.value
        Regex("720", RegexOption.IGNORE_CASE).containsMatchIn(url) -> Qualities.P720.value
        Regex("480", RegexOption.IGNORE_CASE).containsMatchIn(url) -> Qualities.P480.value
        else -> Qualities.Unknown.value
    }
}
