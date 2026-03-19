package com.example.floating

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.util.Log

class ChatAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

        if (event == null) return

        val rootNode = rootInActiveWindow ?: return

        extractText(rootNode)
    }

    private fun extractText(node: AccessibilityNodeInfo?) {
        if (node == null) return

        val text = node.text?.toString()

        if (!text.isNullOrEmpty()) {
            Log.d("TEXT_FOUND", text)

            if (!FloatingDataHolder.messages.contains(text)) {
                FloatingDataHolder.messages.add(text)
            }
        }

        for (i in 0 until node.childCount) {
            extractText(node.getChild(i))
        }
    }

    override fun onInterrupt() {}
}