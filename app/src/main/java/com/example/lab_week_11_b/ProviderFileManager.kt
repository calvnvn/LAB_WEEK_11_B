package com.example.lab_week_11_b

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import org.apache.commons.io.IOUtils // Import dari commons-io
import java.io.File
import java.util.concurrent.Executor

// Helper class to manage files in MediaStore
class ProviderFileManager(
    private val context: Context,
    private val fileHelper: FileHelper,
    private val contentResolver: ContentResolver,
    private val executor: Executor,
    private val mediaContentHelper: MediaContentHelper
) {
    // Generate the data model (FileInfo) for the file
    fun generatePhotoUri(time: Long): FileInfo {
        val name = "img_$time.jpg"
        val file = File(
            context.getExternalFilesDir(fileHelper.getPicturesFolder()),
            name
        )
        return FileInfo(
            fileHelper.getUriFromFile(file),
            file,
            name,
            fileHelper.getPicturesFolder(),
            "image/jpeg"
        )
    }

    fun generateVideoUri(time: Long): FileInfo {
        val name = "video_$time.mp4"
        val file = File(
            context.getExternalFilesDir(fileHelper.getVideosFolder()),
            name
        )
        return FileInfo(
            fileHelper.getUriFromFile(file),
            file,
            name,
            fileHelper.getVideosFolder(),
            "video/mp4"
        )
    }

    // Insert the image/video to MediaStore
    fun insertImageToStore(fileInfo: FileInfo?) {
        fileInfo?.let {
            insertToStore(
                it,
                mediaContentHelper.getImageContentUri(),
                mediaContentHelper.generateImageContentValues(it)
            )
        }
    }

    fun insertVideoToStore(fileInfo: FileInfo?) {
        fileInfo?.let {
            insertToStore(
                it,
                mediaContentHelper.getVideoContentUri(),
                mediaContentHelper.generateVideoContentValues(it)
            )
        }
    }

    // Insert the file to MediaStore using Input/Output Streams
    private fun insertToStore(fileInfo: FileInfo, contentUri: Uri, contentValues: ContentValues) {
        executor.execute {
            val insertedUri = contentResolver.insert(contentUri, contentValues)
            insertedUri?.let { uri ->
                val inputStream = contentResolver.openInputStream(fileInfo.uri)
                val outputStream = contentResolver.openOutputStream(uri)
                inputStream?.use { input ->
                    outputStream?.use { output ->
                        IOUtils.copy(input, output)
                    }
                }
            }
        }
    }
}