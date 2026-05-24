# 手机端通过 QQ 邮箱接收版本更新

## 目标

参照 MatePad 端相框项目做法，手机端新版本通过 QQ 邮箱投递到设备端。

GitHub 负责自动生成 APK。

QQ 邮箱负责发送更新邮件，并把 APK 作为附件发送。

由于手机端不能使用 VPN，不能依赖 GitHub 下载地址，所以手机端更新不再要求访问 GitHub Release。GitHub Release 只作为开发侧备份。

## 核心规则

手机端更新采用：

```text
GitHub Actions 构建 APK
↓
把 APK 复制为 .apk.bin 文件
↓
通过 QQ 邮箱发送 [POSTBIRD_UPDATE] 更新邮件
↓
邮件附件携带 .apk.bin
↓
手机端读取更新邮件附件
↓
保存到本地时改回 .apk
↓
校验 SHA-256
↓
调起系统安装界面
```

## 为什么 APK 附件要加 .bin 后缀

直接发送 `.apk` 附件可能被邮箱或系统安全策略拦截。

所以工作流把：

```text
postbird-mobile-v0.2.0.apk
```

复制为：

```text
postbird-mobile-v0.2.0.apk.bin
```

邮件发送 `.apk.bin` 附件。手机端接收后，再在本地保存为 `.apk` 并调起安装。

## GitHub Actions 工作流

工作流名称：

```text
Publish Mobile Update Email
```

工作流文件：

```text
.github/workflows/publish-mobile-update-email.yml
```

该工作流会自动执行：

1. 构建手机端 APK；
2. 复制 APK 为 `.apk.bin`；
3. 计算 APK 的 SHA-256；
4. 生成 `releases/mobile/latest.json`；
5. 通过 QQ SMTP 发送更新邮件；
6. 把 `.apk.bin` 作为邮件附件；
7. 同时把 APK 上传到 GitHub Release 作为开发侧备份；
8. 上传构建产物用于调试。

## 更新邮件格式

邮件标题：

```text
[POSTBIRD_UPDATE] mobile 0.2.0
```

邮件正文：

```text
POSTBIRD_UPDATE
target=mobile
delivery=qq_email_attachment_bin
versionName=0.2.0
versionCode=2
attachmentName=postbird-mobile-v0.2.0.apk.bin
originalApkName=postbird-mobile-v0.2.0.apk
sha256=APK_SHA256
forceUpdate=false
releaseNotes=手机端自动构建发布版本 0.2.0。APK 已作为 .apk.bin 邮件附件发送。
```

邮件附件：

```text
postbird-mobile-v0.2.0.apk.bin
```

## 需要配置的 GitHub Secrets

需要在 GitHub 仓库中添加以下 Secrets：

```text
QQ_SMTP_USERNAME
QQ_SMTP_AUTH_CODE
POSTBIRD_UPDATE_RECEIVER_EMAIL
```

含义：

```text
QQ_SMTP_USERNAME：发送更新邮件的 QQ 邮箱地址
QQ_SMTP_AUTH_CODE：QQ 邮箱 SMTP 授权码，不是 QQ 登录密码
POSTBIRD_UPDATE_RECEIVER_EMAIL：接收更新邮件的邮箱，通常与 QQ_SMTP_USERNAME 相同
```

不要把这些值写进仓库，也不要发给 ChatGPT。

## 手机端 APP 后续读取规则

手机端 APP 后续应支持：

1. 通过 IMAP 登录绑定 QQ 邮箱；
2. 检索标题包含 `[POSTBIRD_UPDATE]` 的邮件；
3. 只处理 `target=mobile` 的更新邮件；
4. 只处理 `delivery=qq_email_attachment_bin` 的更新方式；
5. 对比本机 `versionCode`；
6. 若邮件中的 `versionCode` 更高，则下载邮件附件 `.apk.bin`；
7. 保存到本地时重命名为 `.apk`；
8. 校验邮件正文中的 `sha256`；
9. 校验通过后调起系统安装界面；
10. 同一版本更新邮件只处理一次。

## 当前策略

手机端不需要访问 GitHub 下载 APK。

GitHub 只负责构建和备份。

QQ 邮箱负责投递更新邮件和 `.apk.bin` 更新附件。
