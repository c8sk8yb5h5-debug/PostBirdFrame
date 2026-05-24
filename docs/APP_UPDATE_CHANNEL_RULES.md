# APP 邮箱更新通道分流规则

本规则同时适用于 MatePad 端项目和手机端项目。两个项目可以复制同一份规则，但每个端必须只识别自己的更新邮件和自己的安装包，避免 MatePad 端误读手机端更新包，或手机端误读 MatePad 端更新包。

## 1. 总原则

MatePad 端和手机端可以共用同一个 QQ 邮箱作为更新通道，但必须使用不同的邮件标题关键字、附件名前缀、包名和版本体系。

APP 内检查更新时，不允许读取通用标题 `PostBirdFrame Update` 作为更新依据。必须读取端类型明确的标题。

GitHub 只作为代码仓库、构建平台和备份发布渠道。APP 内更新只从 QQ 邮箱读取更新包。

## 2. MatePad 端更新规则

MatePad 端只识别 MatePad 专属更新邮件。

邮件标题必须使用：

```text
PostBirdFrame MatePad Update v{versionName} code={versionCode}
```

示例：

```text
PostBirdFrame MatePad Update v1.3.10 code=14
```

附件名必须使用：

```text
PostBird-MatePad-v{versionName}-code{versionCode}.apk.bin
```

示例：

```text
PostBird-MatePad-v1.3.10-code14.apk.bin
```

MatePad 端 APP 扫描更新时，必须同时满足：

```text
1. 邮件标题包含：PostBirdFrame MatePad Update
2. 附件名包含：PostBird-MatePad
3. 附件名后缀为：.apk 或 .apk.bin 或 .apk.zip 或 .zip
4. 发件人等于当前绑定 QQ 邮箱
5. versionCode 大于当前已安装版本
6. applicationId 固定为：com.postbird.frame
7. APK 使用 MatePad 端固定 release keystore 签名
```

不满足以上任一条件，MatePad 端都必须忽略该邮件。

## 3. 手机端更新规则

手机端只识别手机端专属更新邮件。

邮件标题必须使用：

```text
PostBirdFrame Phone Update v{versionName} code={versionCode}
```

示例：

```text
PostBirdFrame Phone Update v1.0.5 code=25
```

附件名必须使用：

```text
PostBird-Phone-v{versionName}-code{versionCode}.apk.bin
```

示例：

```text
PostBird-Phone-v1.0.5-code25.apk.bin
```

手机端 APP 扫描更新时，必须同时满足：

```text
1. 邮件标题包含：PostBirdFrame Phone Update
2. 附件名包含：PostBird-Phone
3. 附件名后缀为：.apk 或 .apk.bin 或 .apk.zip 或 .zip
4. 发件人等于当前绑定 QQ 邮箱
5. versionCode 大于当前已安装版本
6. applicationId 使用手机端固定包名，不得与 MatePad 端相同
7. APK 使用手机端固定 release keystore 签名
```

不满足以上任一条件，手机端都必须忽略该邮件。

## 4. 禁止使用的通用规则

禁止 MatePad 端和手机端继续使用以下通用标题：

```text
PostBirdFrame Update v{versionName} code={versionCode}
```

原因：该标题无法区分 MatePad 端和手机端，容易造成误读。

禁止 MatePad 端和手机端使用相同附件名前缀，例如：

```text
PostBirdFrame-v1.3.10-code14.apk.bin
PostBird-update.apk.bin
latest.apk.bin
```

原因：这些文件名无法稳定判断设备端类型。

## 5. 邮箱扫描策略

APP 检查更新时，应先只读取邮件头信息，快速定位候选邮件，不应一开始读取大附件。

推荐流程：

```text
1. 打开 QQ 邮箱 INBOX
2. 读取最近若干封邮件的标题、发件人和日期
3. 按端类型过滤标题
4. 按 versionCode 判断是否高于当前版本
5. 只对命中的最新一封邮件读取附件
6. 校验附件名前缀和后缀
7. 下载附件
8. 将 .apk.bin 还原为 .apk
9. 调起系统安装界面
```

建议扫描最近 20 封邮件。如果更新邮件经常被其他邮件顶下去，可以提高到 30 封，但不建议无上限扫描。

## 6. versionCode 规则

每个端的 versionCode 独立递增。

MatePad 端示例：

```text
versionCode 14 -> 15 -> 16
versionName 1.3.10 -> 1.3.11 -> 1.3.12
```

手机端示例：

```text
versionCode 25 -> 26 -> 27
versionName 1.0.5 -> 1.0.6 -> 1.0.7
```

APP 判断是否有更新时，只比较本端自己的 versionCode。MatePad 端不得使用手机端 versionCode；手机端不得使用 MatePad 端 versionCode。

## 7. 签名规则

每个端都必须固定 release keystore。

MatePad 端和手机端可以使用不同 keystore，也可以使用同一个 keystore，但一旦确定后不得更换。

覆盖安装是否成功主要取决于：

```text
1. applicationId 不变
2. 签名不变
3. versionCode 递增
```

如果更换 keystore，系统会提示无法覆盖安装，需要卸载旧版。这种情况只能作为一次性迁移，不能作为常规更新方式。

## 8. packageName / applicationId 规则

MatePad 端当前固定为：

```text
com.postbird.frame
```

手机端必须使用另一个固定 applicationId，不得与 MatePad 端相同。

示例：

```text
com.postbird.frame.phone
```

两个端不能使用同一个 applicationId，否则系统可能把它们当作同一个 APP，导致覆盖安装冲突。

## 9. 邮件保留与清理规则

APP 不应自动删除更新邮件。

APP 可以忽略旧版本更新邮件，但不应移动、删除或标记邮件，避免误删用户邮箱内容。

如果邮箱里旧更新邮件太多，可以人工清理。清理时只删除较早版本的更新邮件，保留最近 1 到 2 个版本即可。

## 10. 推荐的 GitHub Actions 输出规则

MatePad 端 workflow 输出：

```text
邮件标题：PostBirdFrame MatePad Update v{versionName} code={versionCode}
附件名称：PostBird-MatePad-v{versionName}-code{versionCode}.apk.bin
正文包含：
platform=matepad
applicationId=com.postbird.frame
versionName={versionName}
versionCode={versionCode}
signing=release
```

手机端 workflow 输出：

```text
邮件标题：PostBirdFrame Phone Update v{versionName} code={versionCode}
附件名称：PostBird-Phone-v{versionName}-code{versionCode}.apk.bin
正文包含：
platform=phone
applicationId=手机端固定 applicationId
versionName={versionName}
versionCode={versionCode}
signing=release
```

## 11. 错误处理规则

如果未找到更新包，应明确提示：

```text
未发现 MatePad 端可用更新包
```

或：

```text
未发现手机端可用更新包
```

不要提示笼统的：

```text
未发现可用更新包
```

如果发现了另一端更新包，应忽略，不要提示安装，不要下载附件。

如果发现本端更新包但 versionCode 不高于当前版本，应提示：

```text
当前已是最新版本
```

如果附件下载成功但安装失败，应提示具体原因，例如：

```text
无法打开系统安装界面
安装包签名与当前版本不一致
安装包版本号未递增
安装包不是当前设备端更新包
```

## 12. 后续开发硬性要求

MatePad 端代码中应使用：

```text
UPDATE_SUBJECT_KEY = "PostBirdFrame MatePad Update"
ATTACHMENT_NAME_KEY = "PostBird-MatePad"
PLATFORM = "matepad"
```

手机端代码中应使用：

```text
UPDATE_SUBJECT_KEY = "PostBirdFrame Phone Update"
ATTACHMENT_NAME_KEY = "PostBird-Phone"
PLATFORM = "phone"
```

禁止两个端共用同一个 `UPDATE_SUBJECT_KEY`。

## 13. 当前结论

MatePad 端和手机端可以共用 QQ 邮箱更新通道，但必须通过“端类型标题 + 端类型附件名前缀 + 独立包名 + 独立 versionCode + 固定 release 签名”完成隔离。

只要严格执行该规则，同一个 QQ 邮箱里同时存在 MatePad 端和手机端更新邮件，也不会互相误读。