package com.xiaoyu.AntiSwipeDismiss

import android.view.KeyEvent
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
class AntiSwipeDismiss : IXposedHookLoadPackage {

    companion object {
        private const val TAG = "AntiSwipeDismiss"
        private const val SYSTEM_GESTURES_CLASS = "com.android.server.policy.SystemGesturesPointerEventListener"
        private const val SIMULATE_KEYSTROKE_METHOD = "simulateKeystroke"
    }
    override fun handleLoadPackage(lpparam: LoadPackageParam) {
        if (lpparam.packageName != "android") {
            return
        }
        log("在系统进程[${lpparam.processName}]中，准备Hook手势监听器。")
        hookSystemGestures(lpparam.classLoader)
    }
    private fun hookSystemGestures(classLoader: ClassLoader) {
        try {
            XposedHelpers.findAndHookMethod(
                SYSTEM_GESTURES_CLASS,
                classLoader,
                SIMULATE_KEYSTROKE_METHOD,
                Int::class.java,
                object : XC_MethodHook() {
                    @Throws(Throwable::class)
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val keyCode = param.args[0] as Int
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            log("已成功拦截右滑退出")
                            param.result = null
                        }
                    }
                }
            )
            log("Hook '$SIMULATE_KEYSTROKE_METHOD' 方法成功！模块已激活。")
        } catch (t: Throwable) {
            logError("Hook失败: ${t.message}")
        }
    }
    private fun log(message: String) {
        XposedBridge.log("[$TAG] $message")
    }
    private fun logError(message: String) {
        XposedBridge.log("[$TAG] ERROR: $message")
    }
}
