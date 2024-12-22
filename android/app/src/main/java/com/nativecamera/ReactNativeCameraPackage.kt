package com.nativecamera

import com.facebook.react.TurboReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.module.model.ReactModuleInfo
import com.facebook.react.module.model.ReactModuleInfoProvider
import com.facebook.react.uimanager.ViewManager
class ReactNativeCameraPackage : TurboReactPackage() {
    override fun createViewManagers(reactContext: ReactApplicationContext): List<ViewManager<*, *>> {
        return listOf(ReactNativeCameraManager(reactContext))
    }

    override fun getModule(s: String, reactApplicationContext: ReactApplicationContext): NativeModule? {
        when (s) {
            ReactNativeCameraManager.REACT_CLASS -> ReactNativeCameraManager(reactApplicationContext)
        }
        return null
    }

    override fun getReactModuleInfoProvider(): ReactModuleInfoProvider = ReactModuleInfoProvider {
        mapOf(ReactNativeCameraManager.REACT_CLASS to ReactModuleInfo(
            _name = ReactNativeCameraManager.REACT_CLASS,
            _className = ReactNativeCameraManager.REACT_CLASS,
            _canOverrideExistingModule = false,
            _needsEagerInit = false,
            isCxxModule = false,
            isTurboModule = true,
        )
        )
    }
}