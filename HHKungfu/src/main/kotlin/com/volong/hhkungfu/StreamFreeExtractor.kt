package com.volong.hhkungfu

import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.USER_AGENT
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.utils.ExtractorApi
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.ExtractorLinkType
import com.lagradost.cloudstream3.utils.Qualities
import com.lagradost.cloudstream3.utils.newExtractorLink
import org.jsoup.nodes.Entities
import java.net.URL

/**
 * HHKUNGFU currently sends its episode player through StreamFree.
 *
 * The important part here is that CloudStream gets the StreamFree embed URL
 * directly and requests it without a browser/devtools environment. The
 * browser page can show StreamFree's anti-devtools warning, while a normal
 * HTTP request can still expose the player configuration.
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
        val requestReferer = referer?.takeIf { it.startsWith("http", true) } ?: mainUrl
        val response = runCatching {
            app.get(
                url,
                referer = requestReferer,
                headers = mapOf(
                    "User-Agent" to USER_AGENT,
                    "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
                    "Accept-Language" to "vi-VN,vi;q=0.9,en-US;q=0.8,en;q=0.7"
                )
            )
        }.getOrNull() ?: return

        val html = Entities.unescape(response.text)
        val candidates = LinkedHashSet<String>()

        // Direct URLs exposed by normal player/source configuration.
        val directRegex = Regex(
            "https?://[^\\s\\\"'<>]+\\.(?:m3u8|mp4)(?:\\?[^\\s\\\"'<>]*)?",
            RegexOption.IGNORE_CASE
        )
        directRegex.findAll(html).forEach { candidates.add(cleanUrl(it.value)) }

        // Common JSON/JWPlayer/source fields, including escaped URLs.
        val fieldRegex = Regex(
            "(?:file|src|source|url|hls|m3u8)\\s*[:=]\\s*[\\\"']([^\\\"']+)\\\"?",
            RegexOption.IGNORE_CASE
        )
        fieldRegex.findAll(html).forEach { match ->
            val value = cleanUrl(match.groupValues[1])
            if (value.contains("m3u8", true) || value.contains(".mp4", true)) {
                candidates.add(value)
            }
        }

        // Some pages store the source in HTML attributes rather than scripts.
        response.document.select("video[src], video source[src], source[src]").forEach { element ->
            val value = cleanUrl(element.attr("src"))
            if (value.isNotBlank()) candidates.add(value)
        }

        // If the source is relative, resolve it against the StreamFree page.
        val resolved = candidates.mapNotNull { resolveUrl(it, url) }.distinct()
        for (streamUrl in resolved) {
            val quality = qualityFromUrl(streamUrl)
            val type = if (streamUrl.contains(".m3u8", true)) {
                ExtractorLinkType.M3U8
            } else {
                ExtractorLinkType.VIDEO
            }
            callback(
                newExtractorLink(
                    source = name,
                    name = "StreamFree ${if (quality != Qualities.Unknown.value) quality.toString() + "p" else "Stream"}",
                    url = streamUrl,
                    type = type
                ) {
                    this.referer = url
                    this.quality = quality
                    this.headers = mapOf("User-Agent" to USER_AGENT)
                }
            )
        }
    }

    private fun cleanUrl(value: String): String = value
        .trim()
        .trim('"', '\'', '`')
        .replace("\\/", "/")
        .replace("&amp;", "&")

    private fun resolveUrl(value: String, base: String): String? {
        if (value.isBlank()) return null
        return runCatching {
            when {
                value.startsWith("http://", true) || value.startsWith("https://", true) -> value
                value.startsWith("//") -> URL(URL(base).protocol, value.removePrefix("//")).toExternalForm()
                else -> URL(URL(base), value).toExternalForm()
            }
        }.getOrNull()
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
