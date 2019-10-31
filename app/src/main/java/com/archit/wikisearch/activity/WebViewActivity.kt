package com.archit.wikisearch.activity

import android.webkit.WebView
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.webkit.WebViewClient
import com.archit.wikisearch.R
import kotlinx.android.synthetic.main.web_view.*

class WebViewActivity : AppCompatActivity() {

    private var pageId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.web_view)

        if(intent.hasExtra("pageid"))
            pageId = intent.getStringExtra("pageid")

        wvBrowser.settings.javaScriptEnabled = true
        wvBrowser.settings.loadWithOverviewMode = true
        wvBrowser.settings.useWideViewPort = true
        wvBrowser.settings.builtInZoomControls = true
        wvBrowser.settings.supportMultipleWindows()
        wvBrowser.webViewClient = OurViewClient()
        wvBrowser.loadUrl("https://en.wikipedia.org/?curid=$pageId")
    }

    override fun onBackPressed() {
        if (wvBrowser.canGoBack()) {
            wvBrowser.goBack()
        } else {
            super.onBackPressed()
        }
    }
}

class OurViewClient : WebViewClient() {
    override fun shouldOverrideUrlLoading(v: WebView, url: String): Boolean {
        v.loadUrl(url)
        return true
    }
}