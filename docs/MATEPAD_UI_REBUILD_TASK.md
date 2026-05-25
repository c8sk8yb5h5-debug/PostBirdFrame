# MatePad UI 重建任务

## 当前结论

`1.4.48` 是功能验证版，不是界面基准。后续不得继续在 `1.4.48` 当前界面上做局部修补。

MatePad 端必须先恢复项目内“MatePad界面优化与预览”的操作界面和交互动效，再叠加已验证的邮件接收、版本更新和播放功能。

## 必须导入的预览源

正式重建前，需要把“MatePad界面优化与预览”中的以下资料放入仓库，作为 Android 实现基准：

```text
1. 预览界面截图
2. React / Canvas 原型代码
3. 已确认的动效规则
4. 左下角圆形控件样式
5. 设置面板样式与出现方式
6. 年份 / 月日选择毛玻璃交互
7. 相框播放区域尺寸与照片比例规则
```

建议保存到：

```text
docs/matepad-preview-source/
```

文件建议：

```text
docs/matepad-preview-source/preview-screenshot-main.png
docs/matepad-preview-source/preview-screenshot-settings.png
docs/matepad-preview-source/preview-screenshot-date-picker.png
docs/matepad-preview-source/MatePadPreviewPrototype.tsx
docs/matepad-preview-source/interaction-notes.md
```

## 重建原则

重建顺序必须是：

```text
1. 先还原预览版视觉骨架
2. 再接入 ReceivedMediaFrame 播放组件
3. 再把 QQ 邮箱接收入口放入预览版设置面板
4. 再把检查更新入口放入预览版设置面板
5. 最后接入自动检查和年月日筛选
```

禁止顺序：

```text
1. 先保留 1.4.48 页面
2. 再逐个调整按钮
3. 再模拟预览版
```

这种方式会继续偏离原型。

## 已验证、可迁移的功能模块

以下功能已经可用，可以迁移到预览版 UI 容器中：

```text
1. QQ 邮箱接收照片 / 视频附件
2. MatePad 专属更新邮件识别
3. QQ 邮箱 APK 更新包下载
4. release keystore 覆盖安装
5. 照片自动播放
6. 视频基础播放
7. 左滑下一张、右滑上一张
8. 设置项加密保存
```

这些功能只作为底层能力迁移，不决定界面样式。

## Android 实现要求

实现时可以保留现有 Kotlin / Jetpack Compose 工程，但必须重写主界面容器。

主界面容器应命名为：

```text
MatePadPreviewHomeScreen
```

预览版设置面板容器应命名为：

```text
MatePadPreviewSettingsPanel
```

预览版时间选择浮层应命名为：

```text
MatePadPreviewDatePickerOverlay
```

播放区域保留或迁移：

```text
ReceivedMediaFrame
```

## 验收标准

下一版 APK 不以功能是否可用作为第一验收标准，而以界面是否回到“MatePad界面优化与预览”为第一验收标准。

最低验收：

```text
1. 打开 APP 后第一眼应接近预览版，而不是 1.4.48 工程页
2. 左下角三个圆形控件大小一致
3. 照片播放区域不被设置面板压缩
4. 设置面板属于预览版浮层，不是普通设置页
5. 年份 / 月日选择有淡毛玻璃遮罩和上下相邻项
6. 播放区不显示工程调试文字
7. 收件和更新功能在设置面板内可用
```
