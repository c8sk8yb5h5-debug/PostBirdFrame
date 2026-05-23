# MatePad 端第五轮开发指令

## 1. 执行目标

本轮在第四轮 QQ 邮箱 IMAP 连接测试基础上，新增只读扫描邮件附件列表能力。

本轮只统计，不下载附件，不修改邮件状态。

## 2. 当前基础状态

第四轮已经完成：

1. QQ 邮箱和授权码加密保存；
2. 设置页可以测试 QQ 邮箱 IMAP 连接；
3. 连接结果会显示在“当前提示”卡片和系统 Toast；
4. 连接测试使用 `Folder.READ_ONLY`；
5. 第四轮提示修复版已在 MatePad 上测试通过。

## 3. 本轮需要实现的内容

新增只读扫描能力：

1. 扫描最近 20 封收件箱邮件；
2. 只统计发件人等于绑定 QQ 邮箱的邮件；
3. 只统计图片和视频附件；
4. 统计图片数量、视频数量和可接收附件总数；
5. 不下载附件；
6. 不读取完整正文；
7. 不删除、移动或标记邮件。

## 4. 新增文件

建议新增：

```text
matepad-app/app/src/main/java/com/postbird/frame/matepad/mail/MailAttachmentScanResult.kt
matepad-app/app/src/main/java/com/postbird/frame/matepad/mail/QqMailAttachmentScanner.kt
```

## 5. 设置页新增功能

设置页新增按钮：

```text
扫描可接收附件
```

点击后显示：

1. 已扫描邮件数量；
2. 来自绑定邮箱的邮件数量；
3. 可接收附件数量；
4. 图片数量；
5. 视频数量；
6. 最近扫描结果。

## 6. 附件格式规则

图片格式：`jpg`、`jpeg`、`png`、`webp`。

视频格式：`mp4`、`mov`、`m4v`。

其他格式忽略。

## 7. 验收标准

1. GitHub Actions 构建成功；
2. APK 可以覆盖安装到 MatePad；
3. “测试邮箱连接”功能仍可用；
4. “扫描可接收附件”按钮可用；
5. 可统计来自绑定邮箱自己的图片和视频附件；
6. 不下载附件；
7. 不修改邮箱邮件状态；
8. 不写入真实邮箱、授权码或 token。
