package com.example.solarmonitor

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.solarmonitor.databinding.ActivityMainBinding

/**
 * Single-Activity, 3-tab monitor for:
 *  - Solarman (globalpro.solarmanpv.com)
 *  - GoodWe SEMS+ (hk-semsplus.goodwe.com)
 *  - DeyeCloud (india.deyecloud.com)
 *
 * All three WebViews are created up front and kept alive in memory; the
 * bottom navigation just toggles visibility, so switching tabs does NOT
 * reload the page or lose your logged-in session. Cookies are persisted
 * to disk so you generally only have to log in to each portal once.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // URL each portal should return to when you tap the toolbar refresh
    // while already on that tab, and the initial load target for each.
    private val homeUrls = mapOf(
        Tab.SOLARMAN to "https://globalpro.solarmanpv.com/",
        Tab.GOODWE to "https://hk-semsplus.goodwe.com/",
        Tab.DEYE to "https://india.deyecloud.com/business/maintain/plant"
    )

    private enum class Tab { SOLARMAN, GOODWE, DEYE }

    private var currentTab: Tab = Tab.SOLARMAN

    private lateinit var webViews: Map<Tab, WebView>
    private lateinit var swipeLayouts: Map<Tab, SwipeRefreshLayout>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        webViews = mapOf(
            Tab.SOLARMAN to binding.webSolarman,
            Tab.GOODWE to binding.webGoodwe,
            Tab.DEYE to binding.webDeye
        )
        swipeLayouts = mapOf(
            Tab.SOLARMAN to binding.swipeSolarman,
            Tab.GOODWE to binding.swipeGoodwe,
            Tab.DEYE to binding.swipeDeye
        )

        setupCookies()
        webViews.forEach { (tab, wv) -> setupWebView(tab, wv) }
        swipeLayouts.forEach { (tab, swipe) ->
            swipe.setOnRefreshListener {
                webViews[tab]?.reload()
            }
        }

        // Load each site once, up front, so switching tabs is instant.
        webViews[Tab.SOLARMAN]?.loadUrl(homeUrls.getValue(Tab.SOLARMAN))
        webViews[Tab.GOODWE]?.loadUrl(homeUrls.getValue(Tab.GOODWE))
        webViews[Tab.DEYE]?.loadUrl(homeUrls.getValue(Tab.DEYE))

        binding.bottomNav.setOnItemSelectedListener { item ->
            val tab = when (item.itemId) {
                R.id.nav_solarman -> Tab.SOLARMAN
                R.id.nav_goodwe -> Tab.GOODWE
                R.id.nav_deye -> Tab.DEYE
                else -> Tab.SOLARMAN
            }
            showTab(tab)
            true
        }

        showTab(Tab.SOLARMAN)
    }

    private fun setupCookies() {
        CookieManager.getInstance().setAcceptCookie(true)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView(tab: Tab, webView: WebView) {
        val settings: WebSettings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.databaseEnabled = true
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = true
        settings.setSupportZoom(true)
        settings.builtInZoomControls = true
        settings.displayZoomControls = false
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        settings.cacheMode = WebSettings.LOAD_DEFAULT

        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)

        webView.webViewClient = object : WebViewClient() {
            // Keep every navigation (login redirects, SSO, etc.) inside the
            // app instead of bouncing out to an external browser.
            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: android.webkit.WebResourceRequest
            ): Boolean {
                view.loadUrl(request.url.toString())
                return true
            }

            override fun onPageFinished(view: WebView, url: String?) {
                super.onPageFinished(view, url)
                swipeLayouts[tab]?.isRefreshing = false
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                if (tab == currentTab) {
                    if (newProgress in 1..99) {
                        binding.progressBar.visibility = android.view.View.VISIBLE
                        binding.progressBar.progress = newProgress
                    } else {
                        binding.progressBar.visibility = android.view.View.GONE
                    }
                }
                if (newProgress >= 100) {
                    swipeLayouts[tab]?.isRefreshing = false
                }
            }

            override fun onReceivedTitle(view: WebView, title: String?) {
                super.onReceivedTitle(view, title)
                if (tab == currentTab) {
                    binding.toolbar.title = title ?: labelFor(tab)
                }
            }
        }
    }

    private fun showTab(tab: Tab) {
        currentTab = tab
        swipeLayouts.forEach { (t, swipe) ->
            swipe.visibility = if (t == tab) android.view.View.VISIBLE else android.view.View.GONE
        }
        binding.toolbar.title = webViews[tab]?.title?.takeIf { it.isNotBlank() } ?: labelFor(tab)
    }

    private fun labelFor(tab: Tab): String = when (tab) {
        Tab.SOLARMAN -> getString(R.string.tab_solarman)
        Tab.GOODWE -> getString(R.string.tab_goodwe)
        Tab.DEYE -> getString(R.string.tab_deye)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_refresh) {
            webViews[currentTab]?.reload()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        val activeWebView = webViews[currentTab]
        if (activeWebView != null && activeWebView.canGoBack()) {
            activeWebView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onPause() {
        super.onPause()
        // Persist cookies (login sessions) to disk.
        CookieManager.getInstance().flush()
    }

    override fun onDestroy() {
        webViews.values.forEach { it.destroy() }
        super.onDestroy()
    }
}
