# mobile-app

本目录用于存放邮差鸟手机端项目代码。

## 端口定位

手机端是文件发送端，主要功能包括：

1. 选择照片或视频；
2. 通过绑定 QQ 邮箱发送到同一个 QQ 邮箱；
3. 邮件标题包含 `[POSTBIRD_FILE]`；
4. 邮件正文写入任务 ID、文件名、文件类型、文件大小、发送时间和 SHA-256；
5. 设置页显示邮箱配置、发送状态和版本更新状态；
6. 通过 GitHub Public Release 检查和下载新版本。

## 自动构建

手机端 APK 通过 GitHub Actions 自动构建。

触发方式：

1. 向 `main` 分支提交 `mobile-app/**` 文件；
2. 在 GitHub Actions 页面手动运行 `Build Mobile APK` 工作流。

构建产物：

```text
postbird-mobile-debug-apk
```

## 安全规则

不得在本目录代码中写入 QQ 邮箱授权码、邮箱密码、GitHub token 或任何个人配置。
