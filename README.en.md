# BluedX

**LSPosed module for Blued** — ad blocking · screenshot unlock · privacy · cleaner feed

[![Build](https://img.shields.io/github/actions/workflow/status/GuiGe101/BluedX/build.yml?style=flat-square)](../../actions)
[![Release](https://img.shields.io/github/v/release/GuiGe101/BluedX?style=flat-square)](../../releases)
[![License](https://img.shields.io/badge/license-MIT-blue?style=flat-square)](LICENSE)

Open-source LSPosed / Xposed module targeting `com.soft.blued`.  
Improves **your local client experience only** — no account cracking, no paid-content unlock.

## Features

| Feature | What it does | Default |
|---------|--------------|---------|
| **Screenshot unlock** | Strips `FLAG_SECURE` so chat / live / photos can be screenshotted | On |
| **Ad blocking** | Filters common ad domains in WebView + OkHttp | On |
| **Analytics blocking** | Blocks umeng / bugly / analytics SDK calls | On |
| **Hide read receipts** | Best-effort interception of read receipts | On |
| **Cleaner feed** | Hides marketing banners / VIP cards | On |
| **Debug logs** | `logcat -s BluedX` | Off |

Toggles live in the module app. Force-stop Blued after changing them.

## Requirements

- Android 8.1+
- LSPosed (Zygisk or Riru)
- Blued (`com.soft.blued`)

## Install

1. Grab `BluedX-x.x.x.apk` from [Releases](../../releases)
2. Install it
3. Enable **BluedX** in LSPosed Manager
4. Scope: `com.soft.blued` (required) and `top.zixuan.bluedx` (optional status indicator)
5. Force-stop Blued
6. Configure switches in BluedX

## Build

```bash
gradle :app:assembleRelease
```

## Disclaimer

For learning Xposed/LSPosed and personal UX tweaks only. No VIP crack, no paid unlock, no stalking tools. Use at your own risk.

## License

[MIT](LICENSE) © GuiGe101

A **Star** is appreciated if this helps you.
