package com.example.communication

import android.util.Log
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.*

const val ENCODING = "UTF-8"

class HttpWrapper(private val URL: String) {

    private lateinit var cookie: String
    private var sessionId = ""

    init {
        CookieHandler.setDefault(CookieManager(null, CookiePolicy.ACCEPT_ALL))
    }

    private fun openConnection(url: String): URLConnection {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.setRequestProperty("Accept-Charset", ENCODING)
        return connection
    }

    fun post(url: String, client_cookie: Int): String {

        this.cookie = client_cookie.toString()

        val connection = openConnection(URL)
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=$ENCODING")

        if(sessionId == "") {
            sessionId = cookie.toString()
            connection.setRequestProperty("Cookie", sessionId)
        }else {
            connection.setRequestProperty("Cookie", sessionId)
        }

        connection.outputStream.use { outputStream ->
            outputStream.write(url.toByteArray(charset(ENCODING)))
        }

        sessionId = connection.getHeaderField("Set-Cookie").split(";")[0]

        connection.inputStream.use { inputStream ->
            return readResponseBody(inputStream, getCharSet(connection))
        }
    }

    fun post(url: String, sessionId: String): String {
        val connection = openConnection(URL)
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=$ENCODING")

        connection.setRequestProperty("Cookie", sessionId)

        connection.outputStream.use { outputStream ->
            outputStream.write(url.toByteArray(charset(ENCODING)))
        }
        connection.inputStream.use { inputStream ->
            return readResponseBody(inputStream, getCharSet(connection))
        }
    }

    private fun readResponseBody(inputStream: InputStream, charset: String?): String {
        var body = ""
        try {
            BufferedReader(InputStreamReader(inputStream, charset)).use { bufferedReader ->
                var line: String?
                do {
                    line = bufferedReader.readLine()
                    body += "$line\n"
                } while (line != null)
            }
        } catch (e: Exception) {
            Log.e("readResponseBody()", e.toString())
            body += "******* Problem reading from server *******\n$e"
        }
        return body
    }

    private fun getCharSet(connection: URLConnection): String? {
        var charset: String? = ENCODING
        val contentType = connection.contentType
        val contentInfo = contentType.replace(" ", "").split(";").toTypedArray()
        for (param in contentInfo) {
            if (param.startsWith("charset=")) charset = param.split("=").toTypedArray()[1]
        }
        Log.i("getCharSet()", "contentType = $contentType")
        Log.i("getCharSet()", "Encoding/charset = $charset")
        return charset
    }

    fun getSessionId(): String {
        return sessionId
    }
}