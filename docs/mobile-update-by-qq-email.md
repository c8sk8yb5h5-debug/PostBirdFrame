# 手机端通过 QQ 邮箱接收版本更新

## 目标

参照 MatePad 端相框项目做法，手机端新版本通过 QQ 邮箱通知设备端。

GitHub 负责自动生成 APK 和发布 Release。

QQ 邮箱负责把新版本信息发送给绑定邮箱。

手机端 APP 后续通过读取邮箱中的 `[POSTBIRD_UPDATE]` 邮件，获得新版本号、APK 下载地址和 SHA-256 校验值，再下载并引导安装。

## 为什么不把 APK 直接作为 QQ 邮件附件

不建议直接把 APK 作为 QQ 邮件附件发送。

原因：

1. APK 文件可能超过普通附件限制；
2. 邮件附件下载和解析更容易受邮箱策略影响；
3. 直接发送 APK 容易被邮箱安全策略拦截；
4. GitHub Release 更适合作为 APK 文件分发源；
5. QQ 邮箱更适合作为“更新通知通道”。

因此采用：

```text
GitHub Release 存放 APK
QQ 邮箱发送更新通知
APP 读取邮件后下载 APK
```

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
2. 创建或更新 GitHub Release；
3. 上传 APK 到 Release；
4. 生成 `releases/mobile/latest.json`；
5. 发送 QQ 邮箱更新邮件；
6. 上传构建产物用于调试。

## 更新邮件格式

邮件标题：

```text
[POSTBIRD_UPDATE] mobile 0.2.0
```

邮件正文：

```text
POSTBIRD_UPDATE
target=mobile
versionName=0.2.0
versionCode=2
apkUrl=https://github.com/xxx/releases/download/mobile-v0.2.0/postbird-mobile-v0.2.0.apk
sha256=APK_SHA256
forceUpdate=false
releaseNotes=手机端自动构建发布版本 0.2.0。
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
4. 对比本机 `versionCode`；
5. 若邮件中的 `versionCode` 更高，则读取 `apkUrl` 下载 APK；
6. 下载完成后校验 `sha256`；
7. 校验通过后调起系统安装界面；
8. 同一版本更新邮件只处理一次。

## 当前策略

QQ 邮箱只作为更新通知通道，不作为 APK 文件存储通道。

APK 文件仍由 GitHub Release 托管。
