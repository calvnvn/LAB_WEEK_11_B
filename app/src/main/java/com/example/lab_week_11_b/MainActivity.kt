package com.example.lab_week_11_b

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_EXTERNAL_STORAGE = 3
    }

    // Helper class to manage files in MediaStore
    private lateinit var providerFileManager: ProviderFileManager

    // Data model for the file
    private var photoInfo: FileInfo? = null
    private var videoInfo: FileInfo? = null

    // Flag to indicate whether the user is capturing a photo or video
    private var isCapturingVideo = false

    // Activity result launcher to capture images and videos
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var takeVideoLauncher: ActivityResultLauncher<Uri>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize the ProviderFileManager
        providerFileManager = ProviderFileManager(
            applicationContext,
            FileHelper(applicationContext),
            contentResolver,
            Executors.newSingleThreadExecutor(),
            MediaContentHelper()
        )

        // Initialize the activity result launcher
        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                providerFileManager.insertImageToStore(photoInfo)
            }
        }

        takeVideoLauncher = registerForActivityResult(ActivityResultContracts.CaptureVideo()) { success ->
            if (success) {
                providerFileManager.insertVideoToStore(videoInfo)
            }
        }

        findViewById<Button>(R.id.photo_button).setOnClickListener {
            isCapturingVideo = false
            checkStoragePermission {
                openImageCapture()
            }
        }

        findViewById<Button>(R.id.video_button).setOnClickListener {
            isCapturingVideo = true
            checkStoragePermission {
                openVideoCapture()
            }
        }
    }

    // Open the camera to capture an image
    private fun openImageCapture() {
        photoInfo = providerFileManager.generatePhotoUri(System.currentTimeMillis())

        // Gunakan .let untuk memastikan photoInfo tidak null saat mengakses uri
        photoInfo?.let { info ->
            takePictureLauncher.launch(info.uri)
        }
    }

    // Open the camera to capture a video
    private fun openVideoCapture() {
        videoInfo = providerFileManager.generateVideoUri(System.currentTimeMillis())

        // Gunakan .let untuk memastikan videoInfo tidak null saat mengakses uri
        videoInfo?.let { info ->
            takeVideoLauncher.launch(info.uri)
        }
    }

    // Check the storage permission
    private fun checkStoragePermission(onPermissionGranted: () -> Unit) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            when (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                PackageManager.PERMISSION_GRANTED -> {
                    onPermissionGranted()
                }
                else -> {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        REQUEST_EXTERNAL_STORAGE
                    )
                }
            }
        } else {
            onPermissionGranted()
        }
    }

    // Handle the permission request result (For Android 9 and below)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_EXTERNAL_STORAGE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    if (isCapturingVideo) {
                        openVideoCapture()
                    } else {
                        openImageCapture()
                    }
                }
            }
        }
    }
}