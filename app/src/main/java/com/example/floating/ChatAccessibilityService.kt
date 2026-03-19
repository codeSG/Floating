package com.example.floating

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.util.Log
import android.os.Handler

class ChatAccessibilityService : AccessibilityService() {

    private val allowedPackages = setOf(
        "com.whatsapp",
        "com.instagram.android"
    )

    private val ignoredWords = setOf(
        "ok", "okay", "yes", "no", "hi", "hello", "hey",
        "bro", "typing", "online", "send", "message",
        "today", "yesterday", "seen", "delivered"
    )

    // ✅ Translation Queue
    private val translationQueue: ArrayDeque<String> = ArrayDeque()
    private var isProcessing = false

    override fun onServiceConnected() {
        super.onServiceConnected()

        EnglishDictionary.load(applicationContext)

        Log.d("SERVICE", "Accessibility Service Connected")

        // ✅ Run initial dummy translations via queue
        runStartupTranslations()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

        if (event == null) return

        val packageName = event.packageName?.toString() ?: return

        // ✅ Only WhatsApp & Instagram
        if (!allowedPackages.contains(packageName)) return

        // ✅ Only useful events
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED &&
            event.eventType != AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED &&
            event.eventType != AccessibilityEvent.TYPE_VIEW_SCROLLED) {
            return
        }

        val rootNode = rootInActiveWindow ?: return

        extractText(rootNode)
    }

    private fun extractText(node: AccessibilityNodeInfo?) {
        if (node == null) return

        val className = node.className?.toString() ?: ""
        val text = node.text?.toString()
        val desc = node.contentDescription?.toString()

        val content = when {
            !text.isNullOrEmpty() -> text
            !desc.isNullOrEmpty() -> desc
            else -> null
        }

        if (!content.isNullOrEmpty()
            && className.contains("TextView")
            && content.length > 2
            && !content.matches(Regex("^\\d{1,2}:\\d{2}.*"))
            && !isLogicalGarbage(content)
            && !isFullyFromDictionary(content)
        ) {

            Log.d("FINAL_MSG", content)

            enqueueTranslation(content)
        }

        // Traverse children
        for (i in 0 until node.childCount) {
            extractText(node.getChild(i))
        }
    }

    // ✅ Add to queue
    private fun enqueueTranslation(text: String) {

        if (translationQueue.contains(text)) return

        translationQueue.add(text)
        Log.d("QUEUE_DEBUG", "Added to queue: $text")
        processNext()
    }

    // ✅ Sequential processor
    private fun processNext() {

        if (isProcessing) return
        if (translationQueue.isEmpty()) return

        val text = translationQueue.removeFirst()
        isProcessing = true

        Log.d("QUEUE_DEBUG", "Processing: $text")
        ApiClient.translate(text) { response ->

            val translatedText = response?.translation ?: "Translation failed"
            val sourceLang = response?.source_language ?: "Unknown"
            val targetLang = response?.target_language ?: "English"

            val finalText = """
                $sourceLang: $text
                $targetLang: $translatedText
            """.trimIndent()
            Log.d("QUEUE_DEBUG", "API response: ${response?.translation}")
            runOnUiThreadSafe {
                if (!FloatingDataHolder.messages.contains(finalText)) {
                    FloatingDataHolder.messages.add(finalText)
                }
            }

            // ✅ Move to next
            isProcessing = false
            processNext()
        }
    }

    // ✅ Startup dummy calls (goes through queue)
    private fun runStartupTranslations() {

        val sampleMessages = listOf(
           // "నువ్వు ఎక్కడ ఉన్నావు",
           // "क्या कर रहे हो",
           // "kalisi veldama",
           // "anna tiffin ayinda",
            "meeru ekkada"
        )

        for (msg in sampleMessages) {
            enqueueTranslation(msg)
        }
    }

    private fun isLogicalGarbage(text: String): Boolean {
        val clean = text.lowercase().trim()

        if (clean.length <= 2) return true

        if (ignoredWords.contains(clean)) return true

        if (clean.matches(Regex("^[^a-zA-Z0-9]+$"))) return true

        val words = clean.split(" ")
        if (words.size <= 2 && words.all { ignoredWords.contains(it) }) return true

        return false
    }

    private fun getWords(text: String): List<String> {
        return text
            .lowercase()
            .replace(Regex("[^a-zA-Z ]"), "")
            .split("\\s+".toRegex())
            .filter { it.isNotBlank() }
    }

    private fun isFullyFromDictionary(text: String): Boolean {

        val words = getWords(text)

        if (words.isEmpty()) return false

        var matchCount = 0

        for (word in words) {
            if (EnglishDictionary.contains(word)) {
                matchCount++
            }
        }

        val ratio = matchCount.toDouble() / words.size

        return ratio > 0.6
    }

    private fun runOnUiThreadSafe(action: () -> Unit) {
        Handler(mainLooper).post {
            action()
        }
    }

    override fun onInterrupt() {}
}