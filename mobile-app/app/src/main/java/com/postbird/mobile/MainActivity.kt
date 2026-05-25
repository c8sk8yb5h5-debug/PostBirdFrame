package com.postbird.mobile

import android.app.Activity
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.concurrent.thread

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
            text = "通过 QQ 邮箱接收 PostBird-Phone 的 .apk.bin 更新包。"
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
                saveMailSettings()
                setStatus("邮箱设置已保存")
            }
        }

        val updateButton = Button(this).apply {
            text = "检查邮箱更新"
            setOnClickListener {
                saveMailSettings()
                checkPhoneUpdate()
            }
        }

        statusText = TextView(this).apply {
            text = "当前版本：${PhoneAppVersion.name(this@MainActivity)} code=${PhoneAppVersion.code(this@MainActivity)}"
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

    private fun saveMailSettings() {
        getSharedPreferences(prefsName, MODE_PRIVATE).edit()
            .putString("mail", emailInput.text.toString().trim())
            .putString("mail_code", codeInput.text.toString().trim())
            .apply()
    }

    private fun checkPhoneUpdate() {
        val address = emailInput.text.toString().trim()
        val pass = codeInput.text.toString().trim()
        if (address.isBlank() || pass.isBlank()) {
            setStatus("请先填写 QQ 邮箱和授权码")
            return
        }
        setStatus("正在检查手机端更新邮件...")
        thread {
            var closeStore: javax.mail.Store? = null
            var closeFolder: javax.mail.Folder? = null
            try {
                val opened = PhoneBoxSession.open(address, pass)
                closeStore = opened.first
                closeFolder = opened.second
                val apk = PhoneUpdateFinder.find(opened.second, cacheDir, PhoneAppVersion.code(this))
                setStatus("更新包已还原，准备打开系统安装界面")
                PhoneUpdateInstaller.install(this, apk) {
                    setStatus("请允许本 APP 安装未知应用，然后再次点击检查邮箱更新")
                }
            } catch (e: Exception) {
                setStatus(e.message ?: "检查邮箱更新失败")
            } finally {
                try { closeFolder?.close(false) } catch (_: Exception) {}
                try { closeStore?.close() } catch (_: Exception) {}
            }
        }
    }

    private fun setStatus(message: String) {
        runOnUiThread { statusText.text = message }
    }
}
