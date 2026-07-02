# Home_mod 中文说明

> 🤖 由 **Gaobaiawa** 与 **DeepSeek V4-Flash**（AI）协作开发

Minecraft 1.20.1 Fabric 模组 — 传送回家 + 倒计时 + 粒子特效。

## 快速链接

- [English README](README.md)
- [最新版发布页](https://github.com/Gaobai-awa/home-mod/releases)
- [反馈问题](https://github.com/Gaobai-awa/home-mod/issues)

---

## 命令

| 命令 | 说明 |
|------|------|
| `/sethome [名字]` | 保存当前位置为家（默认 "home"） |
| `/home [名字]` | 传送回家，带倒计时和粒子效果 |
| `/back` | 返回上次传送前的位置 |
| `/listhomes` | 列出所有已保存的家 |
| `/delhome <名字>` | 删除指定家 |

> **每人最多 100 个家。**

---

## 功能

### 倒计时传送
执行 `/home` 或 `/back` 后，开始 **3 秒倒计时**：
- 屏幕中央显示 Title 提示："3 → 2 → 1"
- 移动超过 0.2 格 → **传送取消**，显示警告消息
- 保持不动 → **传送成功**

### 粒子效果
- `/sethome` — `ENCHANTED_HIT` 金色粒子 + `HAPPY_VILLAGER` 绿色星星
- `/home` 倒计时中 — `END_ROD` 白色粒子持续上升
- `/home` 到达 — `ENCHANTED_HIT` 三层爆发 + `HEART` 爱心粒子
- `/back` 到达 — `END_ROD` + `BUBBLE_POP` 气泡 + `PORTAL` 紫蓝传送门粒子

### 数据存储
- 家数据保存在 `config/homemod/homes.json`
- `/back` 的返回位置自动记录
- 支持跨维度传送

### 其他说明
- 服务端客户端都需要安装此mod — 客户端没装模组只能看到别人传送的粒子效果，但是自己使用不了home相关指令
- 支持跨维度传送（主世界、下界、末地等）

---

## 环境要求

- Minecraft **1.20.1**
- Fabric Loader **0.15+**
- Java **17+**

---

## 安装方法

1. 从 [最新版发布页](https://github.com/Gaobai-awa/home-mod/releases) 下载 `home-mod-1.0.0.jar`
2. 将 JAR 放入服务器、客户端的 `mods` 文件夹
3. 重启服务器即可

---

## 致谢

- **Gaobaiawa** — 设计与测试
- **DeepSeek V4-Flash** — 代码生成与文档

---

## 开源许可

MIT — 详见 [LICENSE](LICENSE)
