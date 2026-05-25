# MatePad 端参考源码索引

本目录用于给手机端项目参考 MatePad 端已经验证过的 QQ 邮箱接收、邮件更新、APK 覆盖安装和构建发布规则。

这些文件是参考副本，不是 MatePad 端实际编译入口。MatePad 端实际源码仍在 `matepad-app/app/` 下。

## 文件对应关系

1. 读取 QQ 邮箱 / IMAP：
   - `QqMailConnectionTester.kt`
   - `MailUpdatePackageDownloader.kt`

2. 扫描更新邮件：
   - `UpdateCoordinator.kt`
   - `MailUpdatePackageDownloader.kt`

3. 解析邮件标题 / 正文：
   - `MailUpdatePackageDownloader.kt`

4. 保存 `.apk.bin` 附件：
   - `MailUpdatePackageDownloader.kt`

5. 还原 `.apk` 并打开安装界面：
   - `MailUpdatePackageInstaller.kt`
   - `ApkInstallHelper.kt`
   - `UpdateActivity.kt`

6. MatePad 端发送更新邮件的 GitHub Actions：
   - `email-matepad-update.yml`

7. MatePad 端构建配置：
   - `build.gradle.kts`

8. MatePad 端清单：
   - `AndroidManifest.xml`

## 手机端复制时必须修改

手机端不能直接照搬 MatePad 的标识。手机端应改为：

```text
邮件标题：PostBirdFrame Phone Update
附件前缀：PostBird-Phone
包名：手机端自己的 applicationId
UI：手机端发送界面，不使用 MatePad 相框播放界面
```

MatePad 端规则保留为：

```text
邮件标题：PostBirdFrame MatePad Update
附件前缀：PostBird-MatePad
```
