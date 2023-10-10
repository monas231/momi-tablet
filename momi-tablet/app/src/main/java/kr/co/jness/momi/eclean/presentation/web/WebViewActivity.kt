package kr.co.jness.momi.eclean.presentation.web

import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import kotlinx.android.synthetic.main.activity_webview.*
import kr.co.jness.momi.eclean.R
import kr.co.jness.momi.eclean.common.BaseActivity

class WebViewActivity: BaseActivity() {

    private val url by lazy {
        intent.getStringExtra("url")
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_webview)

        webView.settings.apply {
            this.javaScriptEnabled = true
            this.javaScriptCanOpenWindowsAutomatically = true
            this.loadsImagesAutomatically = true
            this.useWideViewPort = true
            this.loadWithOverviewMode= true
            this.setSupportZoom(false)
            this.builtInZoomControls = false
            this.cacheMode = WebSettings.LOAD_NO_CACHE
            this.domStorageEnabled = true
            this.allowFileAccess = true
            this.setSupportMultipleWindows(true)
            this.allowContentAccess = true
        }

        webView.webChromeClient = WebChromeClient()

        url?.let {
            webView.loadUrl(it)
        }

    }

    override fun onBackPressed() {
        if(webView.canGoBack()) {
            webView.goBack()
            return
        }
        super.onBackPressed()
    }

}