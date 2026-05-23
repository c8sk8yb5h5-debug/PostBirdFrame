# APK 覆盖安装规则

MatePad 端 App 后续必须支持覆盖安装，避免每次更新都需要删除原 App。

## 固定包名

`applicationId` / `packageName` 必须固定不变：

```text
com.postbird.frame
```

一旦进入正式测试阶段，不允许随意修改包名。

## 固定签名

所有版本必须使用同一个签名文件 keystore。

不允许每次使用不同的 debug 签名或临时签名。

进入正式测试阶段后，必须固定 release keystore。所有 MatePad 端 APK 都必须用同一个 release keystore 构建。

## 递增版本号

每次发布 APK 时必须递增 `versionCode`。

示例：

```text
versionCode 1 -> 2 -> 3 -> 4
```

`versionName` 可以按功能版本变化。

示例：

```text
1.0.0
1.0.1
1.1.0
```

## 无法覆盖安装时的排查顺序

如果出现“无法覆盖安装，需要删除原 App”，优先检查：

1. `packageName` / `applicationId` 是否改变；
2. 签名是否改变；
3. `versionCode` 是否递增；
4. 是否从 debug APK 切换到了 release APK；
5. 是否换了构建电脑或重新生成了签名文件。

## 当前项目注意事项

当前早期测试 APK 使用的是 debug 构建。debug APK 的签名可能受构建环境影响，不适合作为长期覆盖安装方案。

正式进入连续测试前，需要切换为固定 release keystore，并保持同一套签名持续构建。