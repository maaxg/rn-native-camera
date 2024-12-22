package com.nativecamera

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.module.annotations.ReactModule
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.ViewManagerDelegate
import com.facebook.react.viewmanagers.NativeCameraManagerDelegate
import com.facebook.react.viewmanagers.NativeCameraManagerInterface
import com.nativewebview.ReactWebViewManager



@ReactModule(name = ReactWebViewManager.REACT_CLASS)
class ReactNativeCameraManager(context: ReactApplicationContext) :
    SimpleViewManager<ReactNativeCamera>(), NativeCameraManagerInterface<ReactNativeCamera> {

    private val delegate: NativeCameraManagerDelegate<ReactNativeCamera, ReactNativeCameraManager> =
        NativeCameraManagerDelegate(this)

    override fun getDelegate(): ViewManagerDelegate<ReactNativeCamera> = delegate

    override fun createViewInstance(context: ThemedReactContext): ReactNativeCamera {
        return ReactNativeCamera(context)
    }

    override fun getName(): String {
        return REACT_CLASS
    }

    companion object {
        const val REACT_CLASS = "NativeCamera"
    }

    override fun getExportedCustomBubblingEventTypeConstants(): Map<String, Any> =
        mapOf(
            "onScriptLoaded" to
                    mapOf(
                        "phasedRegistrationNames" to
                                mapOf(
                                    "bubbled" to "onScriptLoaded",
                                    "captured" to "onScriptLoadedCapture"
                                )))
}