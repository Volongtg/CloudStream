package com.volong.hhkungfu

import android.content.Context
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.Qualities
import com.lagradost.cloudstream3.utils.loadExtractor
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.net.URLEncoder

class HHKungfuPlugin : Plugin() {
    override fun load(context: Context) {
        registerMainAPI(HHKungfuProvider())
    }
}

class HHKungfuProvider : MainAPI() {
    override var mainUrl = "https://hhkungfu.ee"
    override var name = "HHKUNGFU"
    override var lang = "vi"
    override val hasMainPage = true
    override val hasChromecastSupport = true
    override val supportedTypes = setOf(TvType.Anime, TvType.AnimeMovie, TvType.TvSeries, TvType.Movie)

    private data class Page(val url: String, val name: String)

    private val homePages = listOf(
        Page(mainUrl, "Mới cập nhật"),
        Page("$mainUrl/hoan-thanh", "Hoàn thành"),
        Page("$mainUrl/category/tu-tien", "Tu Tiên"),
        Page("$mainUrl/category/luyen-cap", "Luyện Cấp"),
        Page("$mainUrl/category/trung-sinh", "Trùng Sinh"),
        Page("$mainUrl/category/kiem-hiep", "Kiếm Hiệp"),
        Page("$mainUrl/category/xuyen-khong", "Xuyên Không"),
        Page("$mainUrl/category/hai-huoc", "Hài Hước"),
        Page("$mainUrl/category/hien-dai", "Hiện Đại"),
        Page("$mainUrl/category/ova", "OVA")
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val base = request.data
        val url = if (page <= 1) base else "$base/page/$page"
        val results = parseCards(app.get(url).document)
        return newHomePageResponse(request.name, results, results.isNotEmpty() && page < 30)
    }

    private fun parseCards(document: Document): List<SearchResponse> {
        val seen = HashSet<String>()
        return document.select("a[href]").mapNotNull { a ->
            val href = fixUrlNull(a.attr("href")) ?: return@mapNotNull null
            if (!href.startsWith(mainUrl) || href == mainUrl) return@mapNotNull null
            if (href.contains("/watch-") || href.contains("/category/") || href.contains("/tag/") ||
                href.contains("/author/") || href.contains("/page/") || href.contains("/feed") ||
                href.contains("/contact") || href.contains("/lich-chieu")) return@mapNotNull null

            val image = a.selectFirst("img")?.let { img ->
                val raw = listOf("data-src", "data-lazy-src", "src").firstNotNullOfOrNull { attr ->
                    img.attr(attr).takeIf { it.isNotBlank() }
                }
                raw?.let { fixUrl(it) }
            }
            val title = a.text().replace(Regex("\\s+"), " ").trim()
            if (title.length !in 2..180 || image.isNullOrBlank()) return@mapNotNull null

            val key = href.substringBefore("#").trimEnd('/')
            if (!seen.add(key)) return@mapNotNull null
            newAnimeSearchResponse(cleanTitle(title), key, TvType.Anime) { posterUrl = image }
        }
    }

    private fun cleanTitle(title: String): String = title
        .replace(Regex("^FULL HD\\s*4K?\\s*", RegexOption.IGNORE_CASE), "")
        .replace(Regex("^Trailer\\s*", RegexOption.IGNORE_CASE), "")
        .trim()

    override suspend fun search(query: String): List<SearchResponse> {
        val q = URLEncoder.encode(query, "UTF-8")
        val candidates = listOf("$mainUrl/?s=$q", "$mainUrl/?post_type=post&s=$q", "$mainUrl/search/$q")
        for (url in candidates) {
            val results = runCatching { parseCards(app.get(url).document) }.getOrDefault(emptyList())
            if (results.isNotEmpty()) return results
        }
        return emptyList()
    }

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document
        val title = document.selectFirst("h1")?.text()?.trim()
            ?: document.selectFirst("meta[property='og:title']")?.attr("content")?.trim()
            ?: return null
        val poster = document.selectFirst("meta[property='og:image']")?.attr("content")?.let(::fixUrl)
        val plot = document.selectFirst(".description, .desc, .content-description, .film-description, [class*='description'], [class*='synopsis']")?.text()?.trim()
        val genres = document.select("a[href*='/category/'], a[href*='/the-loai/']")
            .map { it.text().trim() }.filter { it.isNotBlank() }.distinct()

        val episodes = document.select("a[href*='/watch-']").mapNotNull { a ->
            val href = fixUrlNull(a.attr("href")) ?: return@mapNotNull null
            val label = a.text().replace(Regex("\\s+"), " ").trim()
            val number = Regex("(?:Tập|Tap)\\s*([0-9]+)", RegexOption.IGNORE_CASE).find(label)?.groupValues?.getOrNull(1)?.toIntOrNull()
                ?: Regex("tap-([0-9]+)", RegexOption.IGNORE_CASE).find(href)?.groupValues?.getOrNull(1)?.toIntOrNull()
                ?: return@mapNotNull null
            val dubbed = label.contains("thuyết minh", true)
            Episode(href) {
                name = "Tập $number" + if (dubbed) " [Thuyết minh]" else " [Vietsub]"
                episode = number
                season = 1
            }
        }.distinctBy { it.data }

        if (episodes.isEmpty()) {
            return newMovieLoadResponse(cleanTitle(title), url, TvType.AnimeMovie, url) {
                posterUrl = poster
                this.plot = plot
                tags = genres
            }
        }
        return newAnimeLoadResponse(cleanTitle(title), url, TvType.Anime) {
            posterUrl = poster
            this.plot = plot
            tags = genres
            addEpisodes(DubStatus.Subbed, episodes)
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val document = app.get(data).document
        val found = HashSet<String>()

        fun submit(raw: String, referer: String = data) {
            val url = fixUrlNull(raw.trim().trim('"', '\'')) ?: return
            if (!found.add(url)) return
            if (isDirectVideo(url)) emitVideo(url, referer, callback)
            else runCatching { loadExtractor(url, referer, subtitleCallback, callback) }
        }

        // Native video/source tags.
        document.select("video[src], video source[src], source[src]").forEach { submit(it.attr("src")) }

        // Iframes and common data attributes used by embedded players.
        document.select("iframe[src], [data-video], [data-src], [data-url], [data-embed]").forEach { node ->
            val raw = node.attr("src").ifBlank {
                node.attr("data-video").ifBlank {
                    node.attr("data-src").ifBlank { node.attr("data-url").ifBlank { node.attr("data-embed") } }
                }
            }
            if (raw.isNotBlank()) submit(raw)
        }

        // Server buttons: HHKUNGFU currently exposes 1080P/4K V1/V2 choices.
        document.select("a[href], button, [onclick]").forEach { el ->
            val text = el.text()
            val attrText = el.attributes().asList().joinToString(" ") { it.value }
            val combined = "$text $attrText"
            if (Regex("(?:1080P|2160P|4K|V1|V2)", RegexOption.IGNORE_CASE).containsMatchIn(combined)) {
                el.attr("href").takeIf { it.isNotBlank() }?.let(::submit)
                el.attr("data-url").takeIf { it.isNotBlank() }?.let(::submit)
                el.attr("data-video").takeIf { it.isNotBlank() }?.let(::submit)
                el.attr("data-src").takeIf { it.isNotBlank() }?.let(::submit)
                el.attr("onclick").extractUrls().forEach(::submit)
            }
        }

        // Last-resort extraction from inline scripts. This does not bypass DRM/authentication.
        document.select("script:not([src])").forEach { script ->
            Regex("https?://[^\\s\\\"'<>]+(?:m3u8|mp4)(?:\\?[^\\s\\\"'<>]*)?", RegexOption.IGNORE_CASE)
                .findAll(script.data()).forEach { submit(it.value) }
        }
        return found.isNotEmpty()
    }

    private fun String.extractUrls(): List<String> = Regex("https?://[^\\s\\\"'()<>]+", RegexOption.IGNORE_CASE)
        .findAll(this).map { it.value }.toList()

    private fun isDirectVideo(url: String): Boolean = url.contains(Regex("\\.(m3u8|mp4|mkv|webm)(?:\\?|$)", RegexOption.IGNORE_CASE))

    private fun emitVideo(url: String, referer: String, callback: (ExtractorLink) -> Unit) {
        val quality = when {
            url.contains("2160", true) || url.contains("4k", true) -> Qualities.P2160.value
            url.contains("1440", true) -> Qualities.P1440.value
            url.contains("1080", true) -> Qualities.P1080.value
            url.contains("720", true) -> Qualities.P720.value
            url.contains("480", true) -> Qualities.P480.value
            else -> Qualities.Unknown.value
        }
        callback(ExtractorLink(name, name, url, referer, quality, isM3u8 = url.contains(".m3u8", true)))
    }
}
