package com.nativecamera

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.camera.core.AspectRatio
import androidx.core.content.ContextCompat
import com.facebook.react.uimanager.ThemedReactContext
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


class ReactNativeCameraHelper {

    public fun allPermissionsGranted(context: ThemedReactContext) = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            context, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    public fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if(abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

  /*  public fun requestPermissions(context: ThemedReactContext, activity: Activity) {
        val resultLauncher = activity.registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                // Handle Permission granted/rejected
                var permissionGranted = true
                permissions.entries.forEach {
                    if (it.key in REQUIRED_PERMISSIONS && it.value == false)
                        permissionGranted = false
                }
                if (!permissionGranted) {
                    Toast.makeText(context,
                        "Permission request denied",
                        Toast.LENGTH_SHORT).show()
                }

            }

        resultLauncher.launch(REQUIRED_PERMISSIONS)
    }*/

    companion object {
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }
}