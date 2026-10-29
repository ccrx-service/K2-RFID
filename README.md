# K2-RFID 增强版 / CFS RFID 耗材管家

[English](#english-version) | [简体中文](#简体中文版)

---

## 简体中文版

本项目基于 [DnG-Crafts/K2-RFID](https://github.com/DnG-Crafts/K2-RFID) 修改而来。针对 Creality（创想三维）K2 / K1 / HI 打印机的 CFS 耗材系统，我们重构了核心的交互逻辑与 NFC 读写安全机制，**致力于让第三方耗材也能享受到原厂耗材的全部智能体验**。

### 💡 解决的痛点：让第三方耗材不再“失明”
原厂耗材昂贵，而第三方耗材因为缺乏 RFID 标签，插进机器后打印机会直接“失明”：
- ❌ **品牌繁杂不支持**：机器内置库没有的小众品牌无法自动识别。
- ❌ **无法自动调参**：每次都要手动输入温度、流量、压力提前量（PA），选错直接废打。
- ❌ **无法精准追踪余量**：算不出剩余克数，全凭肉眼盲猜。

通过本工具，你可以为第三方耗材“补上身份证”，让打印机将它们视同原厂耗材，恢复所有智能功能。

### ✨ 核心改进与特性

- 🏷️ **支持自定义品牌与自动调参**：配合 PC 端同步工具，将第三方杂牌的参数推送到打印机。贴上标签，机器自动读取专属参数，告别手动输入。
- 📊 **精准余量追踪**：通过写入正确的耗材 ID 与重量数据，CFS 系统的“里程计”可精准计算并显示第三方耗材的真实剩余克数/百分比，告别盲打。
- 🔄 **真·四级联动选择**：主界面严格遵循 **打印机 → 品牌 → 类型 → 名称** 的层级过滤，最终精准映射到唯一的耗材 `base.id`，彻底解决错选、乱选问题。
- 🛡️ **防手抖的“碰一碰”写入**：点击写入后，不再要求手机一直贴着标签。应用会弹窗等待，你拿着手机去碰触标签，识别后自动写入。
- ✅ **回读校验，拒绝假成功**：写入完成后，程序会立即回读 Sector 1 和 2 进行数据比对，只有完全一致才提示成功。若中途移开手机或通信中断，程序会解除旧标签锁定并继续等待重新碰卡；其他写入错误会保留错误提示，允许用户显式重试。
- ⚖️ **预设重量选择**：支持选择 **1 KG、750 G、600 G、500 G、250 G** 五档重量，程序自动转换为标签所需的长度编码。
- ✏️ **支持修改耗材 ID**：编辑页面可直接修改 5 位纯数字耗材 ID。程序会自动同步更新 `kvParam.filament_notes` 中的 `RFID=` 标记，防止被 K2 Pro 固件强制回滚。
- 🔍 **反向智能恢复**：碰触已有标签读取时，通过读取标签上的 ID，自动反向恢复并选中界面上对应的打印机、品牌、类型、名称、重量和颜色。

### 🔗 配套工具：与 K2_Pro_RFID_Material_Sync 协同工作

本应用专注于 **RFID 标签的物理写入与 ID 管理**。要让打印机真正识别并使用你自定义的耗材参数，还需要将耗材配置文件同步到 K2 Pro 的本地数据库中。

配套工具 [**K2_Pro_RFID_Material_Sync**](https://github.com/ccrx-service/K2_Pro_RFID_Material_Sync) 正是为此设计：它负责将切片软件中的耗材配置推送到打印机，而本应用负责把对应的耗材 ID 写入 RFID 标签。

**典型工作流：**
1. 在切片软件（OrcaSlicer / Creality Print）中为第三方耗材配置参数。
2. 使用 **K2_Pro_RFID_Material_Sync** 将参数同步到 K2 Pro 打印机。
3. 使用 **本应用** 选择同一耗材，将相同的 5 位 ID 写入 RFID 标签。
4. 在打印机上刷标签，机器将自动调取并应用对应的耗材参数。

### 📥 下载与安装

请前往 [Releases](https://github.com/ccrx-service/K2-RFID/releases) 页面下载最新版本的 `.apk` 安装包。
> **注意**：由于是侧载应用（Sideloading），安装时若遇系统或 Google Play Protect 安全提示，请选择“了解风险并继续安装”。

### 📱 使用指南

1. **写入标签**：在主界面选好耗材、输入重量后，点击“写入”。不要贴标签，等屏幕弹出等待框后，将手机靠近标签。
2. **读取标签**：将手机靠近已写入的标签，点击“读取”，界面会自动跳转并显示耗材信息。
3. **修改 ID**：点击右上角编辑按钮，在顶部输入新的 5 位数字并保存（确保同步工具也使用相同 ID）。

---

## ⚠️ 免责声明 / Disclaimer

**中文声明：**

1. **非官方声明**：本项目为开源社区爱好者独立开发、维护的第三方工具，与创想三维（Creality）及其关联公司**没有任何关联、授权、合作或认可关系**。本项目不使用、不包含任何创想三维的官方商标、Logo 或专有源代码。
2. **功能边界**：本工具的核心功能仅为**向用户自行拥有的空白/可写 NFC 标签（如 M1 白卡）写入符合特定格式的数据**。本项目**不包含、不提供、也不涉及**任何修改打印机固件、破解系统验证、绕过技术保护措施或非法控制计算机信息系统的功能。
3. **合规使用**：请仅在遵守当地法律法规的前提下，将本工具用于个人合法拥有的耗材与设备的兼容性管理。严禁将本工具用于任何商业侵权、恶意破坏或非法目的。
4. **风险自担**：使用本工具修改标签数据可能导致打印机官方保修失效、耗材数据异常或不可预见的硬件交互问题。使用者需自行承担因使用本工具而产生的全部风险与后果，开发者不对任何直接或间接损失负责。
5. **开源协议**：本项目遵循上游原项目（DnG-Crafts/K2-RFID）的开源协议（GPL-3.0）。您可以自由分发和修改，但必须保留原作者信息并遵循相同的开源协议。

**English Disclaimer:**

1. **Unofficial Statement**: This is an independent, open-source project developed by community enthusiasts. It is **not affiliated with, authorized, endorsed, or sponsored by Creality** or any of its subsidiaries. This project does not use or include any official Creality trademarks, logos, or proprietary source code.
2. **Functional Boundary**: The core function of this tool is solely to **write formatted data to blank/writable NFC tags (e.g., M1 cards) owned by the user**. This project **does NOT** contain, provide, or involve any functionality to modify printer firmware, crack system verification, bypass technical protection measures, or illegally control computer information systems.
3. **Compliant Use**: Please use this tool only for compatibility management of your legally owned consumables and devices, in compliance with local laws and regulations. Commercial infringement, malicious damage, or illegal use is strictly prohibited.
4. **Use at Your Own Risk**: Modifying tag data using this tool may void the official warranty, cause consumable data anomalies, or lead to unforeseen hardware interaction issues. Users assume all risks and consequences arising from the use of this tool. The developers are not liable for any direct or indirect damages.
5. **Open Source License**: This project complies with the open-source license of the upstream original project (DnG-Crafts/K2-RFID) (GPL-3.0]). You are free to distribute and modify it, provided you retain the original author's credits and adhere to the same open-source license.


### 🛠️ 自行编译
本项目使用标准的 Android Gradle 结构。你可以使用 Android Studio 直接导入，或通过命令行编译：
```bash
./gradlew assembleDebug
