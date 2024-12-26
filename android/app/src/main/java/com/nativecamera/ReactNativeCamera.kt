package com.nativecamera

import android.app.Activity
import android.content.ContentValues
import android.net.Uri
import android.util.DisplayMetrics
import android.util.Log
import android.view.OrientationEventListener
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleObserver
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.WritableMap
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.UIManagerHelper
import com.facebook.react.uimanager.events.Event
import com.nativewebview.ReactWebView.OnScriptLoadedEventResult
import okhttp3.internal.applyConnectionSpec
import java.io.File
import java.text.SimpleDateFormat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors





class ReactNativeCamera : FrameLayout, LifecycleObserver {

    private val currentContext: ThemedReactContext
    private var viewFinder: PreviewView
    private var camera: Camera? = null
    private var preview: Preview? = null
    private var orientationListener: OrientationEventListener? = null
    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraHelper: ReactNativeCameraHelper = ReactNativeCameraHelper()
    private var imageCapture: ImageCapture? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var outputPath: String? = null

    constructor(context: ThemedReactContext) : super(context) {
        this.currentContext = context
        this.viewFinder = PreviewView(context)

        viewFinder.layoutParams = LinearLayout.LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT
        )
        installHierarchyFitter(viewFinder)
        addView(viewFinder)
    }

    private fun getActivity() : Activity {
        return currentContext.currentActivity!!;
    }

    private fun installHierarchyFitter(view: ViewGroup) {
        Log.d(TAG, "Looking for ThemedReactContext")

        if(context is ThemedReactContext) {
            Log.d(TAG, "Found ThemedReactContext")
            view.setOnHierarchyChangeListener(object : OnHierarchyChangeListener {
                override fun onChildViewRemoved(parent: View?, child: View?) = Unit
                override fun onChildViewAdded(parent: View?, child: View?) {
                    parent?.measure(
                        MeasureSpec.makeMeasureSpec(measuredWidth, MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(measuredHeight, MeasureSpec.EXACTLY)
                    )
                    parent?.layout(0, 0, parent.measuredWidth, parent.measuredHeight)
                }
            })
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        if (cameraHelper.allPermissionsGranted(currentContext)) {
            viewFinder.post {
                setupCamera()
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        cameraExecutor.shutdown()
        orientationListener?.disable()
        cameraProvider?.unbindAll()
    }

    fun onCameraReady(result: OnScriptLoadedEventResult) {
        val reactContext = context as ReactContext
        val surfaceId = UIManagerHelper.getSurfaceId(reactContext)
        val eventDispatcher = UIManagerHelper.getEventDispatcherForReactTag(
            reactContext, id
        )
        val payload = Arguments.createMap().apply {
            putString("result", result.name)
        }
        val event = OnScriptLoadedEvent(surfaceId, id, payload)
        eventDispatcher?.dispatchEvent(event)
    }


    private fun setupCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(getActivity())
        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            cameraProvider = cameraProviderFuture.get()

            buildCameraUseCases()
        }, ContextCompat.getMainExecutor(getActivity()))

    }


    private fun buildCameraUseCases() {
        if(viewFinder.display == null) return

        val metrics = DisplayMetrics().also { viewFinder.display.getRealMetrics(it) }
        Log.d(TAG, "Screen metrics: ${metrics.widthPixels} x ${metrics.heightPixels}")

        val screenAspectRatio = cameraHelper.aspectRatio(metrics.widthPixels, metrics.heightPixels)

        val rotation = viewFinder.display.rotation
        Log.d(TAG, "Preview aspect ratio: $screenAspectRatio")

        val cameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()


        preview = Preview.Builder()
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(rotation)
            .build()

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(rotation)
            .build()

        imageAnalyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setTargetAspectRatio(screenAspectRatio)
            .build()

        val useCases = mutableListOf(preview, imageCapture)

        cameraProvider?.unbindAll()

        try {
            val newCamera = cameraProvider?.bindToLifecycle(getActivity() as AppCompatActivity, cameraSelector, *useCases.toTypedArray())
            camera = newCamera

            preview?.setSurfaceProvider(viewFinder.surfaceProvider)
        //    onCameraReady(OnScriptLoadedEventResult.success)
        }catch(exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
            onCameraReady(OnScriptLoadedEventResult.error)
        }
    }

    fun capture(options: Map<String, Any>, promise: Promise) {
       val outputPath: String = when {
           outputPath != null -> outputPath!!
           else -> {
               val out = File.createTempFile("ckcap", ".jpg", context.cacheDir)
               out.deleteOnExit()
               out.canonicalPath
           }
       }

        val outputFile = File(outputPath)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()

        imageCapture?.takePicture(
            outputOptions, ContextCompat.getMainExecutor(getActivity()), object : ImageCapture.OnImageSavedCallback {
                override fun onError(ex: ImageCaptureException) {
                    Log.e(TAG, "CameraView: Photo capture failed", ex)
                    promise.reject("E_CAPTURE_FAILED", "takePicture failed: ${ex.message}")
                }

                override fun  onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    try {
                        val uri = outputFileResults.savedUri ?: Uri.fromFile(outputFile)
                        val id = uri?.path
                        val name = uri?.lastPathSegment
                        val path = uri?.path

                        val savedUri = (outputFileResults.savedUri ?: outputPath).toString()

                        val imageInfo = Arguments.createMap()
                        imageInfo.putString("uri", uri.toString())
                        imageInfo.putString("id", id)
                        imageInfo.putString("name", name)
                        imageInfo.putInt("width", width)
                        imageInfo.putInt("height", height)
                        imageInfo.putString("path", path)

                        promise.resolve(imageInfo)
                    }catch (ex: Exception) {
                        Log.e(TAG, "Error while saving or decoding saved photo: ${ex.message}", ex)
                        promise.reject("E_ON_IMG_SAVED", "Error while reading saved photo: ${ex.message}")
                    }
                }
            }
        )

    }


    enum class OnScriptLoadedEventResult() {
        success(),
        error()
    }

    inner class OnScriptLoadedEvent(
        surfaceId: Int,
        viewId: Int,
        private val payload: WritableMap
    ) : Event<OnScriptLoadedEvent>(surfaceId, viewId) {
        override fun getEventName() = "onScriptLoaded"

        override fun getEventData() = payload
    }

    companion object {
        private const val TAG = "REACT_NATIVE_CAMERA"
    }
}