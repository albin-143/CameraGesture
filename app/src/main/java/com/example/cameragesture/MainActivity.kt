package com.example.cameragesture

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {

    private lateinit var status: TextView
    private lateinit var enableBtn: Button
    private lateinit var list: LinearLayout

    private fun dp(x: Int) = (x * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(40), dp(16), dp(24))
        }
        val scroll = ScrollView(this).apply {
            setBackgroundColor(0xFF1E1A18.toInt())
            addView(root)
        }
        root.addView(TextView(this).apply {
            text = "Camera Gesture"
            textSize = 26f
            setTextColor(Color.WHITE)
        })
        status = TextView(this).apply {
            textSize = 15f
            setPadding(0, dp(8), 0, dp(8))
        }
        root.addView(status)
        enableBtn = Button(this).apply {
            text = "Enable service"
            setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        }
        root.addView(enableBtn)
        root.addView(TextView(this).apply {
            text = "GESTURE ACTIONS"
            textSize = 13f
            setTextColor(0xFFFFB59A.toInt())
            setPadding(0, dp(20), 0, dp(8))
        })
        list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(list)
        setContentView(scroll)
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun serviceEnabled(): Boolean {
        val s = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: ""
        return s.contains(packageName) && s.contains("NotchService")
    }

    private fun refresh() {
        val on = serviceEnabled()
        status.text = if (on) "ACTIVE - touch the camera notch" else "Service is OFF"
        status.setTextColor(if (on) 0xFFFFB59A.toInt() else Color.LTGRAY)
        enableBtn.visibility = if (on) View.GONE else View.VISIBLE

        list.removeAllViews()
        for ((key, title) in Gestures.all) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(16), dp(14), dp(16), dp(14))
                background = GradientDrawable().apply {
                    cornerRadius = dp(16).toFloat()
                    setColor(0xFF2A2320.toInt())
                }
                setOnClickListener { pick(key, title) }
            }
            row.addView(TextView(this).apply { text = title; textSize = 18f; setTextColor(Color.WHITE) })
            row.addView(TextView(this).apply {
                text = Actions.label(this@MainActivity, Actions.get(this@MainActivity, key))
                textSize = 14f
                setTextColor(0xFFFFB59A.toInt())
            })
            val lp = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(10) }
            list.addView(row, lp)
        }
    }

    private fun pick(key: String, title: String) {
        val gl = Actions.globals.entries.toList()
        val names = mutableListOf("None", "Open Camera")
        names += gl.map { it.value.first }
        names += "Open app..."
        AlertDialog.Builder(this).setTitle(title).setItems(names.toTypedArray()) { _, i ->
            when {
                i == 0 -> save(key, "none")
                i == 1 -> save(key, "camera")
                i == names.size - 1 -> pickApp(key)
                else -> save(key, gl[i - 2].key)
            }
        }.show()
    }

    private fun pickApp(key: String) {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val apps = packageManager.queryIntentActivities(intent, 0)
            .sortedBy { it.loadLabel(packageManager).toString().lowercase() }
        val labels = apps.map { it.loadLabel(packageManager).toString() }.toTypedArray()
        AlertDialog.Builder(this).setTitle("Choose app").setItems(labels) { _, i ->
            save(key, "app:" + apps[i].activityInfo.packageName)
        }.show()
    }

    private fun save(key: String, value: String) {
        Actions.set(this, key, value)
        refresh()
    }
}
