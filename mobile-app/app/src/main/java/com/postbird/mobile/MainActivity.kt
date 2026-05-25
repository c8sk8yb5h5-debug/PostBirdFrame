package com.postbird.mobile

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import kotlin.concurrent.thread

class MainActivity : Activity() {
    private lateinit var statusText: TextView
    private lateinit var store: MobileSettingsStore
    private var pendingType: String = "photo"
    private var selectedUris: List<Uri> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        store = MobileSettingsStore(this)
        buildHome()
    }

    private fun buildHome() {
        val root = FrameLayout(this)
        root.addView(PostBirdPngSceneView(this), FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))

        val gear = Button(this).apply {
            text = "⚙"
            textSize = 22f
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            setOnClickListener { showSettingsDialog() }
        }
        root.addView(gear, FrameLayout.LayoutParams(dp(58), dp(58), Gravity.TOP or Gravity.END).apply { topMargin = dp(42); rightMargin = dp(34) })

        val photoButton = Button(this).apply { text = ""; alpha = 0.02f; setOnClickListener { openPicker("photo") } }
        root.addView(photoButton, FrameLayout.LayoutParams(dp(130), dp(110), Gravity.BOTTOM or Gravity.START).apply { leftMargin = dp(58); bottomMargin = dp(48) })

        val videoButton = Button(this).apply { text = ""; alpha = 0.02f; setOnClickListener { openPicker("video") } }
        root.addView(videoButton, FrameLayout.LayoutParams(dp(130), dp(110), Gravity.BOTTOM or Gravity.END).apply { rightMargin = dp(58); bottomMargin = dp(48) })

        statusText = TextView(this).apply {
            text = "选择要发送的文件吧！"
            textSize = 13f
            gravity = Gravity.CENTER
            setTextColor(android.graphics.Color.rgb(55, 70, 85))
            setPadding(dp(12), dp(6), dp(12), dp(6))
        }
        root.addView(statusText, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(44), Gravity.BOTTOM).apply { leftMargin = dp(24); rightMargin = dp(24); bottomMargin = dp(8) })
        setContentView(root)
    }

    private fun openPicker(type: String) {
        pendingType = type
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            this.type = if (type == "photo") "image/*" else "video/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
        startActivityForResult(intent, if (type == "photo") REQUEST_PHOTO else REQUEST_VIDEO)
    }

    @Deprecated("Deprecated in Android API")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK || data == null) return
        val result = mutableListOf<Uri>()
        data.clipData?.let { clip -> for (i in 0 until clip.itemCount) result.add(clip.getItemAt(i).uri) }
        data.data?.let { result.add(it) }
        result.forEach { uri -> try { contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) {} }
        selectedUris = result.distinct()
        if (selectedUris.isNotEmpty()) showConfirmDialog()
    }

    private fun showConfirmDialog() {
        val label = if (pendingType == "photo") "照片" else "视频"
        AlertDialog.Builder(this)
            .setTitle("待发送内容")
            .setMessage("已选择 ${selectedUris.size} 个$label。确认后会通过 QQ 邮箱发送给 MatePad。")
            .setNegativeButton("重新选择") { _, _ -> openPicker(pendingType) }
            .setNeutralButton("取消", null)
            .setPositiveButton("确认发送") { _, _ -> sendSelectedMedia() }
            .show()
    }

    private fun sendSelectedMedia() {
        val settings = store.load()
        if (!settings.hasMailConfig) { setStatus("请先打开设置，填写 QQ 邮箱和授权码"); showSettingsDialog(); return }
        val receiver = settings.receiver.ifBlank { settings.email }
        setStatus("正在发送 ${selectedUris.size} 个文件...")
        thread {
            val result = MobileMediaSender.send(this, settings.email, settings.authCode, receiver, selectedUris)
            setStatus(result.message)
            runOnUiThread {
                AlertDialog.Builder(this)
                    .setTitle(if (result.success) "发送成功" else "发送失败")
                    .setMessage(result.message)
                    .setNegativeButton("返回首页", null)
                    .setPositiveButton("继续发送") { _, _ -> openPicker(pendingType) }
                    .show()
            }
        }
    }

    private fun showSettingsDialog() {
        val current = store.load()
        val panel = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(8), dp(4), dp(8), dp(4)) }
        val emailInput = EditText(this).apply { hint = "QQ 邮箱"; setText(current.email); inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS }
        val codeInput = EditText(this).apply { hint = "QQ 邮箱授权码"; setText(current.authCode); inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD }
        val receiverInput = EditText(this).apply { hint = "收件邮箱，不填则发给自己"; setText(current.receiver); inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS }
        val showCode = CheckBox(this).apply {
            text = "显示授权码"
            setOnCheckedChangeListener { _, checked -> codeInput.inputType = if (checked) InputType.TYPE_CLASS_TEXT else InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD; codeInput.setSelection(codeInput.text.length) }
        }
        val versionInfo = TextView(this).apply { text = "当前版本：${PhoneAppVersion.name(this@MainActivity)} code=${PhoneAppVersion.code(this@MainActivity)}"; textSize = 13f; setPadding(0, dp(10), 0, 0) }
        val updateButton = Button(this).apply { text = "检查手机端版本更新"; setOnClickListener { store.save(MobileSettings(emailInput.text.toString(), codeInput.text.toString(), receiverInput.text.toString())); checkPhoneUpdate() } }
        panel.addView(emailInput); panel.addView(codeInput); panel.addView(showCode); panel.addView(receiverInput); panel.addView(versionInfo); panel.addView(updateButton)
        AlertDialog.Builder(this)
            .setTitle("设置")
            .setView(panel)
            .setNegativeButton("取消", null)
            .setPositiveButton("保存") { _, _ -> store.save(MobileSettings(emailInput.text.toString(), codeInput.text.toString(), receiverInput.text.toString())); setStatus("设置已保存") }
            .show()
    }

    private fun checkPhoneUpdate() {
        val settings = store.load()
        if (!settings.hasMailConfig) { setStatus("请先填写 QQ 邮箱和授权码"); return }
        setStatus("正在检查手机端更新邮件...")
        thread {
            var closeStore: javax.mail.Store? = null
            var closeFolder: javax.mail.Folder? = null
            try {
                val opened = PhoneBoxSession.open(settings.email, settings.authCode)
                closeStore = opened.first; closeFolder = opened.second
                val apk = PhoneUpdateFinder.find(opened.second, cacheDir, PhoneAppVersion.code(this))
                setStatus("更新包已还原，准备打开系统安装界面")
                PhoneUpdateInstaller.install(this, apk) { setStatus("请允许本 APP 安装未知应用，然后再次点击检查手机端版本更新") }
            } catch (e: Exception) {
                setStatus(e.message ?: "检查手机端更新失败")
            } finally {
                try { closeFolder?.close(false) } catch (_: Exception) {}
                try { closeStore?.close() } catch (_: Exception) {}
            }
        }
    }

    private fun setStatus(message: String) { runOnUiThread { statusText.text = message; Toast.makeText(this, message, Toast.LENGTH_SHORT).show() } }
    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
    companion object { private const val REQUEST_PHOTO = 301; private const val REQUEST_VIDEO = 302 }
}
