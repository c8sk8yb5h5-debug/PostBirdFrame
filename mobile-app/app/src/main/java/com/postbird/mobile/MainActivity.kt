package com.postbird.mobile

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
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
        val root = FrameLayout(this).apply {
            setBackgroundColor(Color.rgb(245, 240, 232))
        }
        val scene = PostBirdSceneView(this)
        root.addView(scene, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))

        val gear = Button(this).apply {
            text = "⚙"
            textSize = 22f
            setTextColor(Color.rgb(58, 82, 96))
            setBackgroundColor(Color.TRANSPARENT)
            setOnClickListener { showSettingsDialog() }
        }
        root.addView(gear, FrameLayout.LayoutParams(dp(58), dp(58), Gravity.TOP or Gravity.END).apply {
            topMargin = dp(42)
            rightMargin = dp(34)
        })

        val photoButton = Button(this).apply {
            text = ""
            alpha = 0.02f
            setOnClickListener { openPicker("photo") }
        }
        root.addView(photoButton, FrameLayout.LayoutParams(dp(110), dp(88), Gravity.BOTTOM or Gravity.START).apply {
            leftMargin = dp(68)
            bottomMargin = dp(58)
        })

        val videoButton = Button(this).apply {
            text = ""
            alpha = 0.02f
            setOnClickListener { openPicker("video") }
        }
        root.addView(videoButton, FrameLayout.LayoutParams(dp(110), dp(88), Gravity.BOTTOM or Gravity.END).apply {
            rightMargin = dp(68)
            bottomMargin = dp(58)
        })

        statusText = TextView(this).apply {
            text = "选择要发送的文件吧！"
            textSize = 13f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(55, 70, 85))
            setPadding(dp(12), dp(6), dp(12), dp(6))
        }
        root.addView(statusText, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(44), Gravity.BOTTOM).apply {
            leftMargin = dp(24)
            rightMargin = dp(24)
            bottomMargin = dp(8)
        })

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
        data.clipData?.let { clip ->
            for (i in 0 until clip.itemCount) result.add(clip.getItemAt(i).uri)
        }
        data.data?.let { result.add(it) }
        result.forEach { uri ->
            try { contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) {}
        }
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
        if (!settings.hasMailConfig) {
            setStatus("请先打开设置，填写 QQ 邮箱和授权码")
            showSettingsDialog()
            return
        }
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
        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(8), dp(4), dp(8), dp(4))
        }
        val emailInput = EditText(this).apply {
            hint = "QQ 邮箱"
            setText(current.email)
            inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        }
        val codeInput = EditText(this).apply {
            hint = "QQ 邮箱授权码"
            setText(current.authCode)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        val receiverInput = EditText(this).apply {
            hint = "收件邮箱，不填则发给自己"
            setText(current.receiver)
            inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        }
        val showCode = CheckBox(this).apply {
            text = "显示授权码"
            setOnCheckedChangeListener { _, checked ->
                codeInput.inputType = if (checked) InputType.TYPE_CLASS_TEXT else InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                codeInput.setSelection(codeInput.text.length)
            }
        }
        val versionInfo = TextView(this).apply {
            text = "当前版本：${PhoneAppVersion.name(this@MainActivity)} code=${PhoneAppVersion.code(this@MainActivity)}"
            textSize = 13f
            setPadding(0, dp(10), 0, 0)
        }
        val updateButton = Button(this).apply {
            text = "检查手机端版本更新"
            setOnClickListener {
                store.save(MobileSettings(emailInput.text.toString(), codeInput.text.toString(), receiverInput.text.toString()))
                checkPhoneUpdate()
            }
        }

        panel.addView(emailInput)
        panel.addView(codeInput)
        panel.addView(showCode)
        panel.addView(receiverInput)
        panel.addView(versionInfo)
        panel.addView(updateButton)

        AlertDialog.Builder(this)
            .setTitle("设置")
            .setView(panel)
            .setNegativeButton("取消", null)
            .setPositiveButton("保存") { _, _ ->
                store.save(MobileSettings(emailInput.text.toString(), codeInput.text.toString(), receiverInput.text.toString()))
                setStatus("设置已保存")
            }
            .show()
    }

    private fun checkPhoneUpdate() {
        val settings = store.load()
        if (!settings.hasMailConfig) {
            setStatus("请先填写 QQ 邮箱和授权码")
            return
        }
        setStatus("正在检查手机端更新邮件...")
        thread {
            var closeStore: javax.mail.Store? = null
            var closeFolder: javax.mail.Folder? = null
            try {
                val opened = PhoneBoxSession.open(settings.email, settings.authCode)
                closeStore = opened.first
                closeFolder = opened.second
                val apk = PhoneUpdateFinder.find(opened.second, cacheDir, PhoneAppVersion.code(this))
                setStatus("更新包已还原，准备打开系统安装界面")
                PhoneUpdateInstaller.install(this, apk) {
                    setStatus("请允许本 APP 安装未知应用，然后再次点击检查手机端版本更新")
                }
            } catch (e: Exception) {
                setStatus(e.message ?: "检查手机端更新失败")
            } finally {
                try { closeFolder?.close(false) } catch (_: Exception) {}
                try { closeStore?.close() } catch (_: Exception) {}
            }
        }
    }

    private fun setStatus(message: String) {
        runOnUiThread {
            statusText.text = message
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    companion object {
        private const val REQUEST_PHOTO = 301
        private const val REQUEST_VIDEO = 302
    }
}

private class PostBirdSceneView(context: Context) : View(context) {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(45, 56, 70)
        textAlign = Paint.Align.CENTER
        textSize = 38f
        isFakeBoldText = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        val card = RectF(w * 0.065f, h * 0.025f, w * 0.935f, h * 0.975f)
        p.color = Color.rgb(142, 194, 238)
        canvas.drawRoundRect(card, 38f, 38f, p)

        p.color = Color.rgb(190, 225, 245)
        canvas.drawRect(card.left, card.top + card.height() * 0.52f, card.right, card.bottom, p)
        p.color = Color.rgb(192, 220, 128)
        canvas.drawRect(card.left, card.top + card.height() * 0.82f, card.right, card.bottom, p)
        drawCloud(canvas, card.left + card.width() * 0.20f, card.top + card.height() * 0.14f, 1.0f)
        drawCloud(canvas, card.left + card.width() * 0.78f, card.top + card.height() * 0.14f, 0.95f)
        drawCloud(canvas, card.left + card.width() * 0.24f, card.top + card.height() * 0.33f, 0.72f)
        drawCloud(canvas, card.left + card.width() * 0.83f, card.top + card.height() * 0.32f, 0.78f)
        drawSpeech(canvas, card)
        drawBird(canvas, card)
        drawEnvelope(canvas, card.left + card.width() * 0.26f, card.bottom - card.height() * 0.13f, true)
        drawEnvelope(canvas, card.left + card.width() * 0.74f, card.bottom - card.height() * 0.13f, false)
        drawTree(canvas, card.left + card.width() * 0.12f, card.bottom - card.height() * 0.25f, 0.75f)
        drawTree(canvas, card.left + card.width() * 0.88f, card.bottom - card.height() * 0.25f, 0.70f)
    }

    private fun drawCloud(c: Canvas, x: Float, y: Float, s: Float) {
        p.color = Color.WHITE
        p.alpha = 220
        c.drawCircle(x - 22f * s, y + 6f * s, 18f * s, p)
        c.drawCircle(x, y, 24f * s, p)
        c.drawCircle(x + 24f * s, y + 8f * s, 16f * s, p)
        c.drawRoundRect(RectF(x - 42f * s, y + 8f * s, x + 44f * s, y + 24f * s), 12f * s, 12f * s, p)
        p.alpha = 255
    }

    private fun drawSpeech(c: Canvas, card: RectF) {
        val bubble = RectF(card.left + card.width() * 0.19f, card.top + card.height() * 0.18f, card.right - card.width() * 0.19f, card.top + card.height() * 0.255f)
        p.color = Color.WHITE
        c.drawRoundRect(bubble, 42f, 42f, p)
        val tail = Path().apply {
            moveTo(bubble.centerX() - 18f, bubble.bottom - 2f)
            lineTo(bubble.centerX(), bubble.bottom + 28f)
            lineTo(bubble.centerX() + 18f, bubble.bottom - 2f)
            close()
        }
        c.drawPath(tail, p)
        textPaint.textSize = 30f
        c.drawText("选择要发送的文件吧！", bubble.centerX(), bubble.centerY() + 10f, textPaint)
    }

    private fun drawBird(c: Canvas, card: RectF) {
        val cx = card.centerX()
        val cy = card.top + card.height() * 0.58f
        p.color = Color.rgb(67, 87, 91)
        c.drawOval(RectF(cx - 95f, cy - 120f, cx + 105f, cy + 142f), p)
        p.color = Color.rgb(246, 238, 218)
        c.drawOval(RectF(cx - 72f, cy - 112f, cx + 42f, cy + 10f), p)
        p.color = Color.rgb(247, 192, 76)
        c.drawOval(RectF(cx - 168f, cy - 72f, cx - 44f, cy + 8f), p)
        p.color = Color.rgb(35, 46, 49)
        c.drawOval(RectF(cx - 170f, cy - 72f, cx - 102f, cy + 8f), p)
        p.strokeWidth = 8f
        c.drawLine(cx - 104f, cy - 24f, cx - 2f, cy - 24f, p)
        p.style = Paint.Style.FILL
        p.color = Color.BLACK
        c.drawCircle(cx - 14f, cy - 56f, 7f, p)
        p.color = Color.rgb(53, 70, 77)
        c.drawOval(RectF(cx - 45f, cy - 170f, cx + 92f, cy - 105f), p)
        p.color = Color.rgb(245, 190, 70)
        c.drawCircle(cx - 8f, cy - 142f, 10f, p)
        p.color = Color.rgb(235, 225, 205)
        c.drawOval(RectF(cx + 64f, cy + 12f, cx + 136f, cy + 102f), p)
        p.color = Color.rgb(242, 137, 34)
        c.drawRect(cx - 38f, cy + 132f, cx - 22f, cy + 190f, p)
        c.drawRect(cx + 34f, cy + 132f, cx + 50f, cy + 190f, p)
        p.strokeWidth = 8f
        c.drawLine(cx - 54f, cy + 190f, cx - 6f, cy + 190f, p)
        c.drawLine(cx + 18f, cy + 190f, cx + 66f, cy + 190f, p)
        p.style = Paint.Style.FILL
    }

    private fun drawEnvelope(c: Canvas, x: Float, y: Float, photo: Boolean) {
        val r = RectF(x - 58f, y - 35f, x + 58f, y + 35f)
        p.color = Color.rgb(255, 248, 238)
        c.drawRoundRect(r, 10f, 10f, p)
        p.color = Color.rgb(255, 164, 176)
        p.strokeWidth = 4f
        p.style = Paint.Style.STROKE
        c.drawRoundRect(r, 10f, 10f, p)
        p.style = Paint.Style.FILL
        p.color = if (photo) Color.rgb(120, 185, 215) else Color.rgb(80, 95, 112)
        c.drawRoundRect(RectF(x - 30f, y - 20f, x + 30f, y + 20f), 5f, 5f, p)
        if (!photo) {
            p.color = Color.rgb(220, 60, 50)
            val play = Path().apply { moveTo(x - 8f, y - 13f); lineTo(x - 8f, y + 13f); lineTo(x + 16f, y); close() }
            c.drawPath(play, p)
        }
    }

    private fun drawTree(c: Canvas, x: Float, y: Float, s: Float) {
        p.color = Color.rgb(130, 170, 82)
        c.drawCircle(x, y, 25f * s, p)
        p.color = Color.rgb(135, 90, 45)
        c.drawRect(x - 5f * s, y + 15f * s, x + 5f * s, y + 55f * s, p)
    }
}
