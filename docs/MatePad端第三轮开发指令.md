# MatePad 端第三轮开发指令

## 1. 执行目标

本轮在第二轮 MatePad 端设置页本地保存能力基础上，继续升级本地配置安全性。

第三轮目标：

1. 保留设置页现有交互；
2. 保留 QQ 邮箱保存、读取、清除功能；
3. 保留 QQ 邮箱授权码保存、读取、清除功能；
4. 保留自动检查开关保存、读取、清除功能；
5. 将 `MailSettingsStore` 从普通 `SharedPreferences` 升级为加密存储；
6. 优先使用 AndroidX Security Crypto 的 `EncryptedSharedPreferences`；
7. 不改变主界面和设置面板的视觉结构；
8. GitHub Actions 必须构建成功；
9. APK 必须可以覆盖安装到 MatePad；
10. 不得在代码或 GitHub 仓库中写入任何真实邮箱、授权码、GitHub token 或个人配置。

---

## 2. 当前基础状态

第二轮已经完成：

1. `MailSettings` 设置数据模型；
2. `MailSettingsStore` 本地设置存储类；
3. 设置页“保存设置”按钮；
4. 设置页“清除配置”按钮；
5. QQ 邮箱本地保存；
6. QQ 邮箱授权码本地保存；
7. 自动检查开关本地保存；
8. 重启 APP 后读取已保存配置；
9. 第二轮 APK 已在 MatePad 上安装测试通过。

---

## 3. 本轮需要修改的内容

### 3.1 修改依赖

在 `matepad-app/app/build.gradle.kts` 中新增 AndroidX Security Crypto 依赖。

建议新增：

```kotlin
implementation("androidx.security:security-crypto:1.1.0-alpha06")
