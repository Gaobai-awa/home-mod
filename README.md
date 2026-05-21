# Home_mod

> 🤖 Created by **Gaobaiawa** & **DeepSeek V4-Flash** (AI)

A Fabric mod for Minecraft 1.20.1 — Home management with countdown teleport, particle effects, and more.

## Quick Links

- [中文说明 (Chinese README)](README_zh.md)
- [Latest Release](https://github.com/Gaobai-awa/home-mod/releases)
- [Report an Issue](https://github.com/Gaobai-awa/home-mod/issues)

---

## Commands

| Command | Description |
|---------|-------------|
| `/sethome [name]` | Set your home point (default: "home") |
| `/home [name]` | Teleport home with countdown + particles |
| `/back` | Return to your last teleport location |
| `/listhomes` | List all your saved homes |
| `/delhome <name>` | Delete a saved home |

> **Max 100 homes per player.**

---

## Features

### Countdown Teleport
When you run `/home` or `/back`, a **3-second countdown** starts:
- Title overlay shows "3 → 2 → 1" in the center of your screen
- If you move more than 0.2 blocks → **teleport cancelled**, warning message shown
- If you stay still → **teleport completes**

### Particle Effects
- `/sethome` — Golden `ENCHANTED_HIT` + green `HAPPY_VILLAGER` sparkle burst
- `/home` depart — `END_ROD` rising particles during countdown
- `/home` arrive — `ENCHANTED_HIT` layered burst + `HEART` particles
- `/back` arrive — `END_ROD` + `BUBBLE_POP` + `PORTAL` cyan swirl

### Data
- Homes stored in `config/homemod/homes.json` (per-player, per-name)
- Last teleport source position saved automatically for `/back`
- Works across dimensions

### Notes
- Server-side only — effects visible to all players even if they don't have the mod
- Supports cross-dimension teleport

---

## Environment

- Minecraft **1.20.1**
- Fabric Loader **0.15+**
- Java **17+**

---

## Installation

1. Download `home-mod-1.0.0.jar` from the [Latest Release](https://github.com/Gaobai-awa/home-mod/releases)
2. Place the JAR in your server's `mods` folder
3. Restart the server

---

## Credits

- **Gaobaiawa** — Design & Testing
- **DeepSeek V4-Flash** — Code Generation & Documentation

---

## License

MIT — see [LICENSE](LICENSE)
