/**
 * Copyright 2014 DV8FromTheWorld (Austin Keener)
 * Copyright 2023 Luca Argolo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Modifications:
 *  - Ported original code to Kotlin
 *  - Replaced WebException by RuntimeException
 *  - Added delete() function
 */

package io.github.lucaargolo.fabricvision.utils

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.RuntimeException
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Controls all interface with the web and the Imgur API.
 *
 * @author DV8FromTheWorld (Austin Keener)
 * @author Luca Argolo
 * @version v2.0.0  August 17, 2023
 */
object ImgurHelper {

    private const val API_URL = "https://api.imgur.com/3/image"

    private const val CLIENT_ID = "24bce76a03fcf7e"

    /**
     * Takes a file and uploads it to Imgur.
     * Does not check to see if the file is an image, this should be done
     * before the file is passed to this method.
     *
     * @param file
     * The image to be uploaded to Imgur.
     * @return
     * The JSON response from Imgur.
     */
    fun upload(file: File): String {
        val conn: HttpURLConnection = getHttpConnection(API_URL, "POST")
        writeToConnection(conn, "image=" + toBase64(file))
        return getResponse(conn)
    }

    /**
     * Takes the deleteHash string from a previously uploaded image and requests Imgur to delete it.
     *
     * @param hash
     * The deleteHash from an image previously uploaded to Imgur.
     * @return
     * The JSON response from Imgur.
     */
    fun delete(hash: String): String {
        val conn: HttpURLConnection = getHttpConnection("$API_URL/$hash", "DELETE")
        return getResponse(conn)
    }


    /**
     * Converts a file to a Base64 String.
     *
     * @param file
     * The file to be converted.
     * @return
     * The file as a Base64 String.
     */
    @OptIn(ExperimentalEncodingApi::class)
    private fun toBase64(file: File): String {
        val b = ByteArray(file.length().toInt())
        val fs = FileInputStream(file)
        fs.read(b)
        fs.close()
        return URLEncoder.encode(Base64.encode(b), "UTF-8")
    }

    /**
     * Creates and sets up an HttpURLConnection for use with the Imgur API.
     *
     * @return
     * The newly created HttpURLConnection.
     */
    private fun getHttpConnection(url: String, method: String): HttpURLConnection {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.doInput = true
        conn.doOutput = true
        conn.requestMethod = method
        conn.setRequestProperty("Authorization", "Client-ID $CLIENT_ID")
        conn.readTimeout = 100000
        conn.connect()
        return conn
    }

    /**
     * Sends the provided message to the connection as uploaded data.
     *
     * @param conn
     * The connection to send the data to.
     * @param message
     * The data to upload.
     */
    private fun writeToConnection(conn: HttpURLConnection, message: String) {
        val writer = OutputStreamWriter(conn.outputStream)
        writer.write(message)
        writer.flush()
        writer.close()
    }

    /**
     * Gets the response from the connection, Usually in the format of a JSON string.
     *
     * @param conn
     * The connection to listen to.
     * @return
     * The response, usually as a JSON string.
     */
    private fun getResponse(conn: HttpURLConnection): String {
        val str = StringBuilder()
        if (conn.responseCode != 200) {
            throw RuntimeException("Unsuccessful response ${conn.responseCode}")
        }
        val reader = BufferedReader(InputStreamReader(conn.inputStream))
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            str.append(line)
        }
        reader.close()
        if (str.toString() == "") {
            throw RuntimeException("Response was empty")
        }
        return str.toString()
    }

}