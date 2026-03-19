package com.example.floating
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

import org.json.JSONObject

import java.io.IOException
import java.util.concurrent.TimeUnit
object ApiClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private const val URL =
        "https://y5qd9x7fg2.execute-api.eu-north-1.amazonaws.com/translator"

    fun translate(text: String, callback: (TranslateResponse?) -> Unit) {

        val json = JSONObject()
        json.put("text", text)

        val body = json.toString()
            .toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(URL)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.let {
                    val responseStr = it.string()

                    try {
                        val jsonObj = JSONObject(responseStr)

                        val result = TranslateResponse(
                            source_language = jsonObj.getString("source_language"),
                            target_language = jsonObj.getString("target_language"),
                            translation = jsonObj.getString("translation")
                        )

                        callback(result)

                    } catch (e: Exception) {
                        e.printStackTrace()
                        callback(null)
                    }
                }
            }
        })
    }
}