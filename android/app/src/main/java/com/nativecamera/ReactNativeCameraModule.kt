package com.nativecamera

import com.app.specs.NativeCameraModuleSpec
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.uimanager.UIManagerHelper

class ReactNativeCameraModule(private val reactContext: ReactApplicationContext) : NativeCameraModuleSpec(reactContext) {
    companion object {
        // Constants for camera orientation
        /**
         * Represents the portrait orientation with the top of the device up.
         */
        const val PORTRAIT = 0 // ⬆️

        /**
         * Represents the landscape orientation with the left side of the device up.
         */
        const val LANDSCAPE_LEFT = 1 // ⬅️

        /**
         * Represents the portrait orientation with the bottom of the device up.
         */
        const val PORTRAIT_UPSIDE_DOWN = 2 // ⬇️

        /**
         * Represents the landscape orientation with the right side of the device up.
         */
        const val LANDSCAPE_RIGHT = 3 // ➡️

        const val REACT_CLASS = "ReactNativeCameraModule"
    }

    override fun getName(): String {
        return REACT_CLASS
    }
    /**
     * Provides constants related to camera orientation.
     *
     * @return A map containing camera orientation constants.
     */
    override fun getConstants(): Map<String, Any> {
        return hashMapOf(
            "PORTRAIT" to PORTRAIT,
            "PORTRAIT_UPSIDE_DOWN" to PORTRAIT_UPSIDE_DOWN,
            "LANDSCAPE_LEFT" to LANDSCAPE_LEFT,
            "LANDSCAPE_RIGHT" to LANDSCAPE_RIGHT
        )
    }
    @ReactMethod
    override fun capture(options: ReadableMap?, tag: Double?, promise: Promise) {
        val viewTag = tag?.toInt()
        if (viewTag != null && options != null) {
            val uiManager = UIManagerHelper.getUIManagerForReactTag(reactContext, viewTag)
            reactContext.runOnUiQueueThread {
                val camera = uiManager?.resolveView(viewTag) as ReactNativeCamera
                camera.capture(options.toHashMap(), promise)
            }
        }
    }
}