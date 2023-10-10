package kr.co.jness.momi.eclean.module

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import kr.co.jness.momi.eclean.model.FilterWord
import kr.co.jness.momi.eclean.model.Os
import kr.co.jness.momi.eclean.utils.Logger

class BrowserConfig {

    var browserConfigs = loadBrowserConfig()

    private fun loadBrowserConfig() : List<CheckAppConfig> {
        return listOf(
            CheckAppConfig("com.android.chrome", "url_bar"),
            CheckAppConfig("org.mozilla.firefox", "url_bar_title"),
            CheckAppConfig("com.sec.android.app.sbrowser", "location_bar_edit_text"),
            CheckAppConfig("com.naver.whale", "url_bar"),
            CheckAppConfig("com.google.android.youtube", ""),
            CheckAppConfig("com.android.settings", ""),
            CheckAppConfig("com.samsung.accessibility", ""),
            CheckAppConfig("alook.browser", "search_fragment_input_view"),
            CheckAppConfig("com.amazon.cloud9", "url"),
            CheckAppConfig("com.android.browser", "url"),
            CheckAppConfig("com.android.chrome", "url_bar"),
            CheckAppConfig("com.avast.android.secure.browser", "editor"),
            CheckAppConfig("com.avg.android.secure.browser", "editor"),
            CheckAppConfig("com.brave.browser", "url_bar"),
            CheckAppConfig("com.brave.browser_beta", "url_bar"),
            CheckAppConfig("com.brave.browser_default", "url_bar"),
            CheckAppConfig("com.brave.browser_dev", "url_bar"),
            CheckAppConfig("com.brave.browser_nightly", "url_bar"),
            CheckAppConfig("com.chrome.beta", "url_bar"),
            CheckAppConfig("com.chrome.canary", "url_bar"),
            CheckAppConfig("com.chrome.dev", "url_bar"),
            CheckAppConfig("com.cookiegames.smartcookie", "search"),
            CheckAppConfig("com.cookiejarapps.android.smartcookieweb", "mozac_browser_toolbar_url_view"),
            CheckAppConfig("com.duckduckgo.mobile.android", "omnibarTextInput"),
            CheckAppConfig("com.ecosia.android", "url_bar"),
            CheckAppConfig("com.google.android.apps.chrome", "url_bar"),
            CheckAppConfig("com.google.android.apps.chrome_dev", "url_bar"),
            CheckAppConfig("com.jamal2367.styx", "search"),
            CheckAppConfig("com.kiwibrowser.browser", "url_bar"),
            CheckAppConfig("com.microsoft.emmx", "url_bar"),
            CheckAppConfig("com.microsoft.emmx.beta", "url_bar"),
            CheckAppConfig("com.microsoft.emmx.canary", "url_bar"),
            CheckAppConfig("com.microsoft.emmx.dev", "url_bar"),
            CheckAppConfig("com.mmbox.browser", "search_box"),
            CheckAppConfig("com.mmbox.xbrowser", "search_box"),
            CheckAppConfig("com.mycompany.app.soulbrowser", "edit_text"),
            CheckAppConfig("com.naver.whale", "url_bar"),
            CheckAppConfig("com.opera.browser", "url_field"),
            CheckAppConfig("com.opera.browser.beta", "url_field"),
            CheckAppConfig("com.opera.mini.native", "url_field"),
            CheckAppConfig("com.opera.mini.native.beta", "url_field"),
            CheckAppConfig("com.opera.touch", "addressbarEdit"),
            CheckAppConfig("com.qwant.liberty", "mozac_browser_toolbar_url_view"),
            CheckAppConfig("com.qwant.liberty", "url_bar_title"),
            CheckAppConfig("com.sec.android.app.sbrowser", "location_bar_edit_text"),
            CheckAppConfig("com.sec.android.app.sbrowser.beta", "location_bar_edit_text"),
            CheckAppConfig("com.stoutner.privacybrowser.free", "url_edittext"),
            CheckAppConfig("com.stoutner.privacybrowser.standard", "url_edittext"),
            CheckAppConfig("com.vivaldi.browser", "url_bar"),
            CheckAppConfig("com.vivaldi.browser.snapshot", "url_bar"),
            CheckAppConfig("com.vivaldi.browser.sopranos", "url_bar"),
            CheckAppConfig("com.yandex.browser", "bro_omnibar_address_title_text"),
            CheckAppConfig("com.yandex.browser", "bro_omnibox_collapsed_title"),
            CheckAppConfig("com.z28j.feel", "g2"),
            CheckAppConfig("idm.internet.download.manager", "search"),
            CheckAppConfig("idm.internet.download.manager.adm.lite", "search"),
            CheckAppConfig("idm.internet.download.manager.plus", "search"),
            CheckAppConfig("io.github.forkmaintainers.iceraven", "mozac_browser_toolbar_url_view"),
            CheckAppConfig("mark.via", "am"),
            CheckAppConfig("mark.via", "an"),
            CheckAppConfig("mark.via.gp", "as"),
            CheckAppConfig("net.slions.fulguris.full.download", "search"),
            CheckAppConfig("net.slions.fulguris.full.download.debug", "search"),
            CheckAppConfig("net.slions.fulguris.full.playstore", "search"),
            CheckAppConfig("net.slions.fulguris.full.playstore.debug", "search"),
            CheckAppConfig("org.adblockplus.browser", "url_bar"),
            CheckAppConfig("org.adblockplus.browser", "url_bar_title"),
            CheckAppConfig("org.adblockplus.browser.beta", "url_bar"),
            CheckAppConfig("org.adblockplus.browser.beta", "url_bar_title"),
            CheckAppConfig("org.bromite.bromite", "url_bar"),
            CheckAppConfig("org.bromite.chromium", "url_bar"),
            CheckAppConfig("org.chromium.chrome", "url_bar"),
            CheckAppConfig("org.gnu.icecat", "url_bar_title"),
            CheckAppConfig("org.gnu.icecat", "mozac_browser_toolbar_url_view"),
            CheckAppConfig("org.mozilla.fenix", "mozac_browser_toolbar_url_view"),
            CheckAppConfig("org.mozilla.fenix.nightly", "mozac_browser_toolbar_url_view"),
            CheckAppConfig("org.mozilla.fennec_aurora", "mozac_browser_toolbar_url_view"),
            CheckAppConfig("org.mozilla.fennec_aurora", "url_bar_title"),
            CheckAppConfig("org.mozilla.fennec_fdroid", "mozac_browser_toolbar_url_view"),
            CheckAppConfig("org.mozilla.fennec_fdroid", "url_bar_title"),
            CheckAppConfig("org.mozilla.firefox", "mozac_browser_toolbar_url_view"),
            CheckAppConfig("org.mozilla.firefox", "url_bar_title"),
            CheckAppConfig("org.mozilla.firefox_beta", "mozac_browser_toolbar_url_view"),
            CheckAppConfig("org.mozilla.firefox_beta", "url_bar_title"),
            CheckAppConfig("org.mozilla.focus", "display_url"),
            CheckAppConfig("org.mozilla.klar", "display_url"),
            CheckAppConfig("org.mozilla.reference.browser", "mozac_browser_toolbar_url_view"),
            CheckAppConfig("org.mozilla.rocket", "display_url"),
            CheckAppConfig("org.torproject.torbrowser", "mozac_browser_toolbar_url_view"),
            CheckAppConfig("org.torproject.torbrowser", "url_bar_title"),
            CheckAppConfig("org.torproject.torbrowser_alpha", "mozac_browser_toolbar_url_view"),
            CheckAppConfig("org.torproject.torbrowser_alpha", "url_bar_title"),
            CheckAppConfig("org.ungoogled.chromium.extensions.stable", "url_bar"),
            CheckAppConfig("org.ungoogled.chromium.stable", "url_bar"),
            CheckAppConfig("acr.browser.barebones", "search"),
            CheckAppConfig("acr.browser.lightning", "search"),
            CheckAppConfig("com.feedback.browser.wjbrowser", "addressbar_url"),
            CheckAppConfig("com.ghostery.android.ghostery", "search_field"),
            CheckAppConfig("com.htc.sense.browser", "title"),
            CheckAppConfig("com.jerky.browser2", "enterUrl"),
            CheckAppConfig("com.ksmobile.cb", "address_bar_edit_text"),
            CheckAppConfig("com.linkbubble.playstore", "url_text"),
            CheckAppConfig("com.mx.browser", "address_editor_with_progress"),
            CheckAppConfig("com.mx.browser.tablet", "address_editor_with_progress"),
            CheckAppConfig("com.nubelacorp.javelin", "enterUrl"),
            CheckAppConfig("jp.co.fenrir.android.sleipnir", "url_text"),
            CheckAppConfig("jp.co.fenrir.android.sleipnir_black", "url_text"),
            CheckAppConfig("jp.co.fenrir.android.sleipnir_test", "url_text"),
            CheckAppConfig("mobi.mgeek.TunnyBrowser", "title"),
            CheckAppConfig("org.iron.srware", "url_bar")
        )
    }

    fun getAppConfig(packageName: String) : CheckAppConfig? {
        for (config in browserConfigs) {
            if (config.packageName == packageName) {
                return config
            }
        }
        return null
    }
}

data class CheckAppConfig(
    val packageName: String,
    val editTextId: String
) {
    val addressBarId = "$packageName:id/$editTextId"
}