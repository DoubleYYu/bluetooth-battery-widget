# 🔋 蓝牙设备电量小部件 (Bluetooth Battery Widget)

适用于 **小米 17 Pro (HyperOS)** 的蓝牙设备电量查看小部件，可添加到**桌面**或**负一屏**。

## ✨ 功能

- 📱 查看所有已连接蓝牙设备的电量
- 🎧 自动识别设备类型（耳机、手表、音箱、键盘、鼠标）
- 🔄 自动刷新（蓝牙连接/断开时触发）
- 🌙 支持深色模式
- 🎨 iOS 风格设计：圆角卡片 + 毛玻璃效果 + 简洁设备列表

## 📐 布局参考

参考 iOS 蓝牙电量小部件设计：
- 圆角半透明卡片背景
- 设备图标（灰色圆形底）+ 设备名 + 电量进度条 + 百分比
- 顶部标题栏 + 刷新按钮
- 底部更新时间

## 🛠 构建

### 方式一：Android Studio
1. 用 Android Studio 打开项目目录
2. Sync Gradle
3. Build → Build Bundle(s) / APK(s) → Build APK(s)

### 方式二：命令行
```bash
cd bluetooth-battery-widget
./gradlew assembleDebug
# APK 输出路径: app/build/outputs/apk/debug/app-debug.apk
```

## 📱 安装 & 使用

1. 安装 APK 到小米 17 Pro
2. 打开 app，授予蓝牙权限
3. 长按桌面空白处 → 小部件 → 找到「蓝牙设备电量」
4. 拖动到桌面或负一屏
5. 点击右上角 🔄 可手动刷新

## 📁 项目结构

```
app/src/main/
├── AndroidManifest.xml
├── java/com/example/btbattery/
│   ├── MainActivity.kt              # 主界面（权限申请 + 设置）
│   ├── BatteryWidgetProvider.kt     # 小部件核心逻辑
│   ├── BatteryUpdateService.kt      # 后台刷新服务
│   └── BluetoothStateReceiver.kt    # 蓝牙状态监听（自动刷新）
├── res/
│   ├── layout/
│   │   ├── widget_battery.xml       # 小部件主布局（iOS 风格）
│   │   ├── item_device.xml          # 单个设备行布局
│   │   ├── item_no_device.xml       # 空状态布局
│   │   └── activity_main.xml        # 主界面布局
│   ├── drawable/
│   │   ├── bg_widget.xml            # 卡片背景（浅色）
│   │   ├── bg_device_icon.xml       # 设备图标圆底
│   │   ├── bg_battery_progress.xml  # 电量进度条
│   │   ├── ic_*.xml                 # 各类图标
│   │   └── widget_preview.xml       # 小部件预览图
│   ├── drawable-night/
│   │   └── bg_widget.xml            # 卡片背景（深色）
│   ├── xml/
│   │   └── battery_widget_info.xml  # 小部件元数据
│   └── values/
│       ├── strings.xml
│       ├── colors.xml
│       └── themes.xml
```

## ⚙️ 兼容性

- **最低版本**: Android 9 (API 28)
- **目标版本**: Android 15 (API 35)
- **适配**: HyperOS 负一屏、小米桌面小部件

## 📝 注意事项

- 蓝牙电量获取依赖设备支持（Android 10+ 支持标准 Battery Level API）
- 部分蓝牙设备可能不报告电量，此时显示为 `-1`（未知）
- 小米 17 Pro 为假设机型，需确认实际 API 兼容性
