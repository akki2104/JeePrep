package com.jeeprep.app.ui.components

import android.annotation.SuppressLint
import android.util.Log
import android.view.MotionEvent
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

private class WebViewState {
    var pageLoaded = false
    var pendingJs: String? = null
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MathText(
    text: String,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    var heightDp by remember { mutableStateOf(40) }

    val escapedText = remember(text) {
        text.replace("\\", "\\\\")
            .replace("'", "\\'")
            .replace("\n", "\\n")
            .replace("\r", "")
    }

    // Track last sent text to avoid redundant JS calls from height-triggered recompositions
    var lastSentText by remember { mutableStateOf("") }

    AndroidView(
        factory = { context ->
            // Subclass WebView to fully disable touch handling — lets parent Card receive clicks
            object : WebView(context) {
                @SuppressLint("ClickableViewAccessibility")
                override fun onTouchEvent(event: MotionEvent?): Boolean = false
            }.apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false
                isFocusable = false
                isFocusableInTouchMode = false

                webChromeClient = object : WebChromeClient() {
                    override fun onConsoleMessage(msg: ConsoleMessage): Boolean {
                        if (msg.messageLevel() == ConsoleMessage.MessageLevel.ERROR) {
                            Log.d("JeePrep", "WebView JS [${msg.messageLevel()}]: ${msg.message()}")
                        }
                        return true
                    }
                }

                addJavascriptInterface(object {
                    @JavascriptInterface
                    fun onHeightChanged(h: Int) {
                        post { heightDp = h }
                    }
                }, "Android")

                tag = WebViewState()

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        val state = tag as? WebViewState ?: return
                        state.pageLoaded = true
                        val pending = state.pendingJs
                        if (!pending.isNullOrEmpty()) {
                            view?.evaluateJavascript(pending, null)
                        }
                    }
                }

                loadUrl("file:///android_asset/math_renderer.html")
            }
        },
        update = { wv ->
            // Only send JS call when text actually changes
            if (escapedText != lastSentText) {
                lastSentText = escapedText
                val jsCall = "updateContent('" + escapedText + "'," + isDark + ")"
                val state = wv.tag as? WebViewState
                state?.pendingJs = jsCall
                if (state?.pageLoaded == true) {
                    wv.evaluateJavascript(jsCall, null)
                }
                // If page not loaded yet, onPageFinished will pick up the pending call
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .height(heightDp.dp + 4.dp)
            .defaultMinSize(minHeight = 24.dp)
    )
}
