package com.example.floating

import android.content.Context

object EnglishDictionary {

    private val words = HashSet<String>()

    fun load(context: Context) {
        val input = context.assets.open("words.txt")

        input.bufferedReader().useLines { lines ->
            lines.forEach {
                val word = it.trim().lowercase()
                if (word.isNotEmpty()) {
                    words.add(word)
                }
            }
        }
    }

    fun contains(word: String): Boolean {
        return words.contains(word.lowercase())
    }
}