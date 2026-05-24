# 手机端 APK 自动生成与发布流程

## 当前目标

手机端参照 MatePad 端相框项目做法，通过 GitHub 自动生成并发布 APK。

开发者不需要每次手动下载构建产物再重新整理发布文件。

## 工作流

GitHub Actions 工作流：

```text
Publish Mobile APK
```

触发方式：

1. 手动运行；
2. 向 `main` 分支提交 `mobile-app/**` 文件；
3. 修改 `.github/workflows/publish-mobile-apk.yml`。

## 自动执行内容

工作流会自动执行：

1. 拉取仓库代码；
2. 设置 Java 17；
3. 设置 Gradle 8.9；
4. 构建手机端 APK；
5. 根据 `mobile-app/app/build.gradle.kts` 读取 `versionName` 和 `versionCode`；
6. 创建或更新 GitHub Release；
7. 上传 APK 到 Release；
8. 生成 `releases/mobile/latest.json`；
9. 把最新 `latest.json` 提交回仓库；
10. 同时上传 APK artifact，便于调试下载。

## APP 内更新路径

手机端 APP 读取：

```text
https://raw.githubusercontent.com/c8sk8yb5h5-debug/PostBirdFrame/main/releases/mobile/latest.json
```

`latest.json` 内包含：

```text
versionName
versionCode
apkUrl
sha256
forceUpdate
releaseNotes
```

APP 检测到 `versionCode` 更高时，会下载 APK、校验 SHA-256，并调起系统安装界面。

## 注意事项

当前自动发布流程优先保证“通过 GitHub 生成和发布 APK”。

若要长期稳定覆盖安装，仍建议后续切换到固定 release 签名。固定签名需要 GitHub Secrets 或其他安全签名方案。

不要把 QQ 邮箱授权码、邮箱密码、GitHub token 或签名密钥写入仓库。
