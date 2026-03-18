package com.jeeprep.app.ai

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

object ModelDownloadManager {

    private const val MODEL_FILENAME = "gemma-4-E2B-it.litertlm"
    private const val MODEL_DIR = "models"

    // HuggingFace direct download URL for the LiteRT-LM model
    private const val MODEL_URL =
        "https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm/resolve/main/gemma-4-E2B-it.litertlm"

    fun getModelFile(context: Context): File {
        val modelDir = File(context.filesDir, MODEL_DIR)
        return File(modelDir, MODEL_FILENAME)
    }

    fun isModelDownloaded(context: Context): Boolean {
        return getModelFile(context).exists()
    }

    fun getModelSizeBytes(): Long = 2_580_000_000L // ~2.58 GB

    fun downloadModel(context: Context): Flow<DownloadProgress> = flow {
        val modelFile = getModelFile(context)
        modelFile.parentFile?.mkdirs()

        val tempFile = File(modelFile.parent, "${MODEL_FILENAME}.tmp")

        try {
            val url = URL(MODEL_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 30_000
            connection.readTimeout = 30_000

            // Resume support
            var downloadedBytes = 0L
            if (tempFile.exists()) {
                downloadedBytes = tempFile.length()
                connection.setRequestProperty("Range", "bytes=$downloadedBytes-")
            }

            connection.connect()

            val totalBytes = if (downloadedBytes > 0) {
                connection.getHeaderField("Content-Range")
                    ?.substringAfter("/")?.toLongOrNull()
                    ?: (connection.contentLengthLong + downloadedBytes)
            } else {
                connection.contentLengthLong
            }

            emit(DownloadProgress.Downloading(downloadedBytes, totalBytes))

            val input = connection.inputStream
            val output = java.io.FileOutputStream(tempFile, downloadedBytes > 0)

            val buffer = ByteArray(8192)
            var bytesRead: Int

            output.use { out ->
                input.use { inp ->
                    while (inp.read(buffer).also { bytesRead = it } != -1) {
                        out.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead
                        emit(DownloadProgress.Downloading(downloadedBytes, totalBytes))
                    }
                }
            }

            // Rename temp to final
            tempFile.renameTo(modelFile)
            emit(DownloadProgress.Complete)

        } catch (e: Exception) {
            emit(DownloadProgress.Error(e.message ?: "Download failed"))
        }
    }.flowOn(Dispatchers.IO)

    sealed class DownloadProgress {
        data class Downloading(val bytesDownloaded: Long, val totalBytes: Long) : DownloadProgress() {
            val progressPercent: Int get() = if (totalBytes > 0) ((bytesDownloaded * 100) / totalBytes).toInt() else 0
            val downloadedMB: Long get() = bytesDownloaded / (1024 * 1024)
            val totalMB: Long get() = totalBytes / (1024 * 1024)
        }
        data object Complete : DownloadProgress()
        data class Error(val message: String) : DownloadProgress()
    }
}
