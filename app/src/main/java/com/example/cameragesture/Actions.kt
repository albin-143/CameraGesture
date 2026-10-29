package com.example.cameragesture

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.provider.MediaStore

object Gestures {
    val all = listOf(
        "single_tap" to "Single Tap",
        "double_tap" to "Double Tap",
        "long_press" to "Long Press",
        "swipe_left" to "Swipe Left",
        "swipe_right" to "Swipe Right",
        "swipe_down" to "Swipe Down",
    )
}

object Actions {
    val globals = linkedMapOf(
        "global:back" to Pair("Back", AccessibilityService.GLOBAL_ACTION_BACK),
        "global:home" to Pair("Home", AccessibilityService.GLOBAL_ACTION_HOME),
        "global:recents" to Pair("Recents", AccessibilityService.GLOBAL_ACTION_RECENTS),
        "global:notifications" to Pair("Notifications", AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS),
        "global:quick" to Pair("Quick settings", AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS),
        "global:lock" to Pair("Lock screen", AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN),
        "global:screenshot" to Pair("Screenshot", AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT),
    )

    private fun prefs(c: Context) = c.getSharedPreferences("cg", Context.MODE_PRIVATE)

    fun get(c: Context, gesture: String): String =
        prefs(c).getString(gesture, if (gesture == "single_tap") "camera" else "none")!!

    fun set(c: Context, gesture: String, value: String) =
        prefs(c).edit().putString(gesture, value).apply()

    fun label(c: Context, v: String): String = when {
        v == "camera" -> "Open Camera"
        v.startsWith("global:") -> globals[v]?.first ?: "Not set"
        v.startsWith("app:") -> try {
            val pm = c.packageManager
            "Open " + pm.getApplicationLabel(pm.getApplicationInfo(v.substring(4), 0))
        } catch (e: Exception) { "App not found" }
        else -> "Not set"
    }

    fun run(s: AccessibilityService, v: String) {
        try {
            when {
                v == "camera" -> s.startActivity(
                    Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
                v.startsWith("global:") -> globals[v]?.let { s.performGlobalAction(it.second) }
                v.startsWith("app:") -> s.packageManager.getLaunchIntentForPackage(v.substring(4))?.let {
                    it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    s.startActivity(it)
                }
            }
        } catch (e: Exception) { /* ignore */ }
    }
}
