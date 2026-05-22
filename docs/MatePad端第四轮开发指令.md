# MatePad 端第四轮开发指令

## 1. 执行目标

本轮在第三轮“设置页加密保存”基础上，继续接入 QQ 邮箱 IMAP 连接测试。

第四轮目标不是自动下载附件，而是先验证：

1. APP 可以读取本机加密保存的 QQ 邮箱；
2. APP 可以读取本机加密保存的 QQ 邮箱授权码；
3. 设置页新增“测试邮箱连接”按钮；
4. 点击按钮后尝试连接 QQ 邮箱 IMAP 服务；
5. 连接成功时显示“邮箱连接成功”；
6. 连接失败时显示失败原因；
7. 连接测试不得打断主界面；
8. 不得下载附件；
9. 不得删除、移动或修改邮箱中的任何邮件；
10. 不得在代码或 GitHub 仓库中写入真实邮箱、授权码、GitHub token 或个人配置。

---

## 2. 当前基础状态

第三轮已经完成：

1. MatePad 端 Android Kotlin + Jetpack Compose 项目；
2. APP 默认横屏；
3. 设置页可以打开和关闭；
4. QQ 邮箱可以输入；
5. QQ 邮箱授权码可以输入；
6. 设置项已通过 `EncryptedSharedPreferences` 加密保存；
7. 重启 APP 后可以读取设置；
8. 清除配置功能正常；
9. GitHub Actions 构建成功；
10. 第三轮 APK 已在 MatePad 上安装测试通过。

---

## 3. 本轮需要实现的内容

### 3.1 新增邮箱连接测试能力

新增邮箱连接测试类。

建议文件：

```text
matepad-app/app/src/main/java/com/postbird/frame/matepad/mail/QqMailConnectionTester.kt
