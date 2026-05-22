# MatePad 端第二轮开发指令

## 1. 执行目标

本轮在第一轮 MatePad 端 MVP 骨架基础上，继续完善设置页的本地保存能力。

本轮目标不是接入真实邮箱收发，而是先完成：

1. 设置页 QQ 邮箱输入内容可保存；
2. QQ 邮箱授权码输入内容可保存；
3. 自动检查开关状态可保存；
4. 重启 APP 后可以读取已保存配置；
5. 设置页显示保存成功状态；
6. 不得在代码或 GitHub 仓库中写入任何真实邮箱、授权码、GitHub token 或个人配置。

---

## 2. 开发前必须阅读

开始修改代码前，请先阅读：

1. `README.md`
2. `docs/Codex执行指令.md`
3. `docs/技术选型与包名规则.md`
4. `docs/MatePad端MVP任务清单.md`
5. `docs/UI交互说明.md`
6. `docs/发送与更新规则.md`
7. `docs/MatePad端第一轮开发指令.md`
8. `docs/开发记录.md`

---

## 3. 当前基础状态

第一轮已经完成：

1. MatePad 端 Android Kotlin + Jetpack Compose 项目骨架；
2. 包名：`com.postbird.frame.matepad`；
3. APP 名称：`邮差鸟相框`；
4. 默认横屏；
5. 空状态界面；
6. 左下角设置按钮；
7. 设置面板打开和关闭；
8. GitHub Actions 云端构建；
9. APK 已在 MatePad 上安装成功。

---

## 4. 本轮需要实现的内容

### 4.1 新增设置数据模型

新增设置数据结构，用于保存：

1. QQ 邮箱；
2. QQ 邮箱授权码；
3. 自动检查开关；
4. 上次保存时间。

建议命名：

```text
MailSettings
