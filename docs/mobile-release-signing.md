# 手机端 Release APK 固定签名配置

## 目的

手机端如果需要通过 APP 内“检查更新”实现覆盖安装，后续每一个 APK 必须使用同一个签名文件构建。

Debug APK 只适合测试，不适合长期联网更新。

## GitHub Secrets

需要在 GitHub 仓库中添加以下 4 个 Secrets：

```text
MOBILE_RELEASE_KEYSTORE_BASE64
MOBILE_RELEASE_STORE_PASSWORD
MOBILE_RELEASE_KEY_ALIAS
MOBILE_RELEASE_KEY_PASSWORD
```

这些值不得写入仓库，不得提交到 GitHub 文件中，也不要发送给 ChatGPT。

## 推荐流程

1. 在本地电脑生成一个 release keystore。
2. 把 keystore 文件转换成 base64 文本。
3. 把 base64 文本填入 `MOBILE_RELEASE_KEYSTORE_BASE64`。
4. 把 keystore 密码填入 `MOBILE_RELEASE_STORE_PASSWORD`。
5. 把 key alias 填入 `MOBILE_RELEASE_KEY_ALIAS`。
6. 把 key password 填入 `MOBILE_RELEASE_KEY_PASSWORD`。
7. 运行 GitHub Actions 中的 `Build Mobile Release APK`。
8. 下载 `postbird-mobile-release-apk`。

## 后续更新规则

同一台手机上，只有包名相同且签名相同的 APK 才能覆盖更新。

当前手机端包名：

```text
com.postbird.mobile
```

请勿随意修改包名，否则无法覆盖安装旧版本。
