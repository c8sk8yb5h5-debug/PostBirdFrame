package com.postbird.mobile

import android.app.Activity
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    private lateinit var statusText: TextView
    private lateinit var emailInput: EditText
    private lateinit var codeInput: EditText

    private val prefsName = "postbird_mobile_settings"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences(prefsName, MODE_PRIVATE)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 32, 32, 32)
        }

        val title = TextView(this).apply {
            text = "PostBird Mobile"
            textSize = 26f
            gravity = Gravity.CENTER
        }

        val subtitle = TextView(this).apply {
            text = "通过 QQ 邮箱接收 .apk.bin 更新包。"
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(0, 20, 0, 20)
        }

        emailInput = EditText(this).apply {
            hint = "QQ邮箱"
            setText(prefs.getString("mail", "") ?: "")
            inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        }

        codeInput = EditText(this).apply {
            hint = "QQ邮箱授权码"
            setText(prefs.getString("mail_code", "") ?: "")
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        val saveButton = Button(this).apply {
            text = "保存邮箱设置"
            setOnClickListener {
                prefs.edit()
                    .putString("mail", emailInput.text.toString().trim())
                    .putString("mail_code", codeInput.text.toString().trim())
                    .apply()
                setStatus("邮箱设置已保存")
            }
        }

        val updateButton = Button(this).apply {
            text = "检查邮箱更新"
            setOnClickListener {
                prefs.edit()
                    .putString("mail", emailInput.text.toString().trim())
                    .putString("mail_code", codeInput.text.toString().trim())
                    .apply()
                setStatus("邮箱更新接收器正在接入中")
            }
        }

        statusText = TextView(this).apply {
            text = "当前版本：0.3.0"
            textSize = 14f
            gravity = Gravity.CENTER
            setPadding(0, 24, 0, 0)
        }

        root.addView(title)
        root.addView(subtitle)
        root.addView(emailInput)
        root.addView(codeInput)
        root.addView(saveButton)
        root.addView(updateButton)
        root.addView(statusText)
        setContentView(root)
    }

    private fun setStatus(message: String) {
        runOnUiThread { statusText.text = message }
    }
}
