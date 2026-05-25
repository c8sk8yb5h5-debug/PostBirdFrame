# PostBirdFrame APK 生成逻辑统一规则

本文件用于固定 MatePad 端和手机端 APK 的生成、签名、命名、邮件发送和版本识别规则。后续不要再手动修改 `versionCode`、`versionName` 或更新邮件标题。

## 1. 总原则

APK 生成流程必须统一为：

```text
GitHub Actions 触发
↓
使用 github.run_number 自动生成 versionCode
↓
拼接 versionName
↓
通过 Gradle -P 参数注入版本号
↓
使用固定 release keystore 签名
↓
复制 release APK
↓
重命名为 .apk.bin 附件
↓
发送 QQ 邮箱更新邮件
↓
App 内扫描邮件标题和附件名
↓
下载 .apk.bin
↓
还原 .apk
↓
打开系统安装界面
```

禁止再使用以下方式：

```text
手动修改 build.gradle.kts
手动修改 PhoneUpdateRules.kt
手动改 workflow 里的 versionCode
手动改邮件标题里的 code
每个测试版本新建一个固定版本号 workflow
```

## 2. 版本号规则

### 2.1 MatePad 端

```yaml
env:
  MATEPAD_VERSION_CODE: ${{ github.run_number }}
  MATEPAD_VERSION_NAME: 1.4.${{ github.run_number }}
```

Gradle 构建参数：

```bash
gradle -p matepad-app clean assembleRelease \
  -PmatepadVersionCode=$MATEPAD_VERSION_CODE \
  -PmatepadVersionName=$MATEPAD_VERSION_NAME
```

### 2.2 手机端

```yaml
env:
  PHONE_VERSION_CODE: ${{ github.run_number }}
  PHONE_VERSION_NAME: 0.5.${{ github.run_number }}
```

Gradle 构建参数：

```bash
gradle -p mobile-app clean assembleRelease \
  -PphoneVersionCode=$PHONE_VERSION_CODE \
  -PphoneVersionName=$PHONE_VERSION_NAME
```

## 3. build.gradle.kts 规则

### 3.1 MatePad 端

`matepad-app/app/build.gradle.kts` 必须读取外部参数：

```kotlin
val matepadVersionCode = (project.findProperty("matepadVersionCode") as String?)?.toIntOrNull() ?: 1
val matepadVersionName = (project.findProperty("matepadVersionName") as String?) ?: "1.0.0"

android {
    defaultConfig {
        versionCode = matepadVersionCode
        versionName = matepadVersionName
    }
}
```

### 3.2 手机端

`mobile-app/app/build.gradle.kts` 必须读取外部参数：

```kotlin
val phoneVersionCode = (project.findProperty("phoneVersionCode") as String?)?.toIntOrNull() ?: 4
val phoneVersionName = (project.findProperty("phoneVersionName") as String?) ?: "0.4.0"

android {
    defaultConfig {
        versionCode = phoneVersionCode
        versionName = phoneVersionName
    }
}
```

## 4. release 签名规则

MatePad 端和手机端必须使用同一套固定 release keystore。否则系统会提示无法覆盖安装。

GitHub Actions Secrets 必须包含：

```text
POSTBIRD_KEYSTORE_BASE64
POSTBIRD_KEYSTORE_PASSWORD
POSTBIRD_KEY_ALIAS
POSTBIRD_KEY_PASSWORD
```

Actions 中还原 keystore：

```bash
echo "${{ secrets.POSTBIRD_KEYSTORE_BASE64 }}" | base64 --decode > release-keystore.jks
cat > release-signing.properties <<EOF
storeFile=release-keystore.jks
storePassword=${{ secrets.POSTBIRD_KEYSTORE_PASSWORD }}
keyAlias=${{ secrets.POSTBIRD_KEY_ALIAS }}
keyPassword=${{ secrets.POSTBIRD_KEY_PASSWORD }}
EOF
```

## 5. APK 附件命名规则

### 5.1 MatePad 更新包

```text
PostBird-MatePad-v{versionName}-code{versionCode}.apk.bin
```

示例：

```text
PostBird-MatePad-v1.4.188-code188.apk.bin
```

邮件标题：

```text
PostBirdFrame MatePad Update v{versionName} code={versionCode}
```

### 5.2 手机端更新包

```text
PostBird-Phone-v{versionName}-code{versionCode}.apk.bin
```

示例：

```text
PostBird-Phone-v0.5.188-code188.apk.bin
```

邮件标题：

```text
PostBirdFrame Phone Update v{versionName} code={versionCode}
```

## 6. 邮件正文规则

更新邮件正文至少包含：

```text
platform=matepad 或 phone
applicationId=实际 applicationId
versionName=版本名
versionCode=版本号
attachmentName=附件名
signing=release
forceUpdate=false
```

推荐 MatePad 正文：

```text
platform=matepad
applicationId=com.postbird.frame
versionName=${MATEPAD_VERSION_NAME}
versionCode=${MATEPAD_VERSION_CODE}
attachmentName=PostBird-MatePad-v${MATEPAD_VERSION_NAME}-code${MATEPAD_VERSION_CODE}.apk.bin
signing=release
forceUpdate=false
```

推荐手机端正文：

```text
platform=phone
applicationId=com.postbird.mobile
versionName=${PHONE_VERSION_NAME}
versionCode=${PHONE_VERSION_CODE}
attachmentName=PostBird-Phone-v${PHONE_VERSION_NAME}-code${PHONE_VERSION_CODE}.apk.bin
signing=release
forceUpdate=false
```

## 7. App 内更新识别规则

App 内检查更新时，只需要比较：

```text
邮件 versionCode > 当前安装包 versionCode
```

当前安装包版本必须从系统读取，不允许写死：

```kotlin
val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
val currentCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
    packageInfo.longVersionCode.toInt()
} else {
    @Suppress("DEPRECATION")
    packageInfo.versionCode
}
```

## 8. 手机端独立规则

手机端只能识别手机端更新包：

```text
Subject key: PostBirdFrame Phone Update
Attachment key: PostBird-Phone
ApplicationId: com.postbird.mobile
```

手机端不得扫描或安装 MatePad 更新包。

## 9. MatePad 端独立规则

MatePad 端只能识别 MatePad 更新包：

```text
Subject key: PostBirdFrame MatePad Update
Attachment key: PostBird-MatePad
ApplicationId: com.postbird.frame
```

MatePad 端不得扫描或安装手机端更新包。

## 10. 生成后验证清单

每次生成 APK 后检查：

```text
1. versionCode 是否自动递增
2. versionName 是否随 run_number 自动变化
3. APK 是否 release 签名
4. 附件名是否为 .apk.bin
5. 邮件标题是否包含正确平台和 code
6. App 内是否能识别 code 更高的新版本
7. 安装时是否可以覆盖旧版本
```

## 11. 当前推荐 workflow 文件

MatePad：

```text
.github/workflows/email-matepad-update.yml
```

手机端：

```text
.github/workflows/email-phone-update.yml
```

后续不再使用固定版本号测试 workflow，例如：

```text
publish-phone-045-test.yml
```

这类文件只作为历史记录，不再继续维护。
